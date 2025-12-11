package tqs.backend.tqsbackend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import tqs.backend.tqsbackend.entity.Booking;
import tqs.backend.tqsbackend.entity.BookingStatus;
import tqs.backend.tqsbackend.entity.Category;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.entity.PaymentStatus;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@ActiveProfiles("dev")
class BookingRepositoryTest {

        @Autowired
        private BookingRepository bookingRepository;

        @Autowired
        private ItemRepository itemRepository;

        @Autowired
        private CategoryRepository categoryRepository;

        @Test
        void existsByItemIdDetectsOverlap() {
                Category category = categoryRepository.save(new Category("Lighting"));
                Item item = new Item("Vintage Lamp", "A nice lamp", 40.0, category, 4.5, "Lisbon", 200L);
                item = itemRepository.save(item);

                LocalDate start = LocalDate.now().plusDays(5);
                LocalDate end = LocalDate.now().plusDays(8);
                Booking existing = new Booking(item, 300L, start, end, BigDecimal.valueOf(item.getPrice()),
                                BigDecimal.valueOf(item.getPrice()).multiply(BigDecimal.valueOf(3)),
                                BookingStatus.CONFIRMED,
                                PaymentStatus.PAID);
                bookingRepository.save(existing);

                boolean overlap = bookingRepository.existsByItemIdAndStatusInAndStartDateLessThanAndEndDateGreaterThan(
                                item.getId(), List.of(BookingStatus.CONFIRMED, BookingStatus.PENDING), end.minusDays(1),
                                start.plusDays(1));
                boolean noOverlap = bookingRepository
                                .existsByItemIdAndStatusInAndStartDateLessThanAndEndDateGreaterThan(
                                                item.getId(), List.of(BookingStatus.CONFIRMED, BookingStatus.PENDING),
                                                end.plusDays(2), end.plusDays(1));

                assertThat(overlap).isTrue();
                assertThat(noOverlap).isFalse();
        }

        @Test
        void testFindPastRentalsByOwnerId() {
                Category category = categoryRepository.save(new Category("Electronics"));
                Item item = new Item("Test Item", "A test item", 50.0, category, 4.0, "Porto", 100L);
                item = itemRepository.save(item);

                // Create past booking (endDate < today)
                LocalDate pastStart = LocalDate.now().minusDays(10);
                LocalDate pastEnd = LocalDate.now().minusDays(5);
                Booking pastBooking = new Booking(item, 200L, pastStart, pastEnd, BigDecimal.valueOf(50.0),
                                BigDecimal.valueOf(250.0), BookingStatus.CONFIRMED, PaymentStatus.PAID);
                bookingRepository.save(pastBooking);

                // Create upcoming booking (endDate >= today) - should not be returned
                LocalDate futureStart = LocalDate.now().plusDays(5);
                LocalDate futureEnd = LocalDate.now().plusDays(10);
                Booking futureBooking = new Booking(item, 300L, futureStart, futureEnd, BigDecimal.valueOf(50.0),
                                BigDecimal.valueOf(250.0), BookingStatus.CONFIRMED, PaymentStatus.PAID);
                bookingRepository.save(futureBooking);

                List<Booking> pastRentals = bookingRepository
                                .findByItem_OwnerIdAndStatusInAndEndDateLessThanOrderByStartDateDesc(
                                                100L, List.of(BookingStatus.CONFIRMED, BookingStatus.PENDING),
                                                LocalDate.now());

                assertThat(pastRentals).hasSize(1);
                assertThat(pastRentals.get(0).getId()).isEqualTo(pastBooking.getId());
                assertThat(pastRentals.get(0).getEndDate()).isBefore(LocalDate.now());
        }
}
