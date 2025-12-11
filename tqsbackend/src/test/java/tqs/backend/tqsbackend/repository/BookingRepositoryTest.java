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
                BigDecimal.valueOf(item.getPrice()).multiply(BigDecimal.valueOf(3)), BookingStatus.ACCEPTED,
                PaymentStatus.PAID);
        bookingRepository.save(existing);

        boolean overlap = bookingRepository.existsByItemIdAndStatusInAndStartDateLessThanAndEndDateGreaterThan(
                item.getId(), List.of(BookingStatus.ACCEPTED, BookingStatus.REQUESTED), end.minusDays(1), start.plusDays(1));
        boolean noOverlap = bookingRepository.existsByItemIdAndStatusInAndStartDateLessThanAndEndDateGreaterThan(
                item.getId(), List.of(BookingStatus.ACCEPTED, BookingStatus.REQUESTED), end.plusDays(2), end.plusDays(1));

        assertThat(overlap).isTrue();
        assertThat(noOverlap).isFalse();
    }
}
