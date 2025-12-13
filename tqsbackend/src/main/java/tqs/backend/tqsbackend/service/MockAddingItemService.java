package tqs.backend.tqsbackend.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tqs.backend.tqsbackend.entity.Booking;
import tqs.backend.tqsbackend.entity.BookingStatus;
import tqs.backend.tqsbackend.entity.Category;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.entity.PaymentStatus;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.repository.BookingRepository;
import tqs.backend.tqsbackend.repository.CategoryRepository;
import tqs.backend.tqsbackend.repository.ItemRepository;
import tqs.backend.tqsbackend.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class MockAddingItemService {

        private final ItemRepository itemRepository;
        private final CategoryRepository categoryRepository;
        private final UserRepository userRepository;
        private final BookingRepository bookingRepository;

        @Value("${MOCK_USER_PASSWORD:testPassword123}")
        private String mockUserPassword;

        @PostConstruct
        public void init() {
                if (userRepository.count() == 0 && itemRepository.count() == 0) {
                        // Create owner users
                        User owner1 = new User("Maria Silva", "maria@ua.pt",
                                        BCrypt.hashpw(mockUserPassword, BCrypt.gensalt()),
                                        UserRoles.OWNER);
                        User owner2 = new User("João Santos", "joao@ua.pt",
                                        BCrypt.hashpw(mockUserPassword, BCrypt.gensalt()),
                                        UserRoles.OWNER);
                        User owner3 = new User("Ana Costa", "ana@ua.pt",
                                        BCrypt.hashpw(mockUserPassword, BCrypt.gensalt()),
                                        UserRoles.OWNER);

                        // Create renter users
                        User renter1 = new User("Carlos Ferreira", "carlos@ua.pt",
                                        BCrypt.hashpw(mockUserPassword, BCrypt.gensalt()),
                                        UserRoles.RENTER);
                        User renter2 = new User("Sofia Martins", "sofia@ua.pt",
                                        BCrypt.hashpw(mockUserPassword, BCrypt.gensalt()),
                                        UserRoles.RENTER);
                        User renter3 = new User("Pedro Oliveira", "pedro@ua.pt",
                                        BCrypt.hashpw(mockUserPassword, BCrypt.gensalt()),
                                        UserRoles.RENTER);

                        userRepository.saveAll(Arrays.asList(owner1, owner2, owner3, renter1, renter2, renter3));

                        // Create categories
                        Category electronics = new Category("Electronics");
                        Category lighting = new Category("Lighting");
                        Category furniture = new Category("Furniture");
                        categoryRepository.saveAll(Arrays.asList(electronics, lighting, furniture));

                        // Create items (reduced to 5 items)
                        Item partyLamps = new Item("Party Lights", "Beautiful Colorful Lights", 55.0, lighting, 0.0,
                                        "Lisbon");
                        partyLamps.setOwnerId(owner1.getId());
                        itemRepository.save(partyLamps);

                        Item mealTable = new Item("Party Meal Table", "Foldable dining table", 150.0, furniture, 0.0,
                                        "Porto");
                        mealTable.setOwnerId(owner1.getId());
                        itemRepository.save(mealTable);

                        Item audioInterface = new Item("Audio Interface", "High performance laptop", 1200.0,
                                        electronics, 0.0,
                                        "Lisbon");
                        audioInterface.setOwnerId(owner2.getId());
                        itemRepository.save(audioInterface);

                        Item sunHat = new Item("Big Sun Hat", "Sun Hat for Parties", 80.0, furniture, 0.0, "Lisbon");
                        sunHat.setOwnerId(owner2.getId());
                        itemRepository.save(sunHat);

                        Item speaker = new Item("Bluetooth Speaker", "Portable speaker", 40.0, electronics, 0.0,
                                        "Porto");
                        speaker.setOwnerId(owner3.getId());
                        itemRepository.save(speaker);

                        // Create past bookings (completed rentals)
                        Booking pastBooking1 = new Booking(partyLamps, renter1.getId(),
                                        LocalDate.now().minusDays(20), LocalDate.now().minusDays(15),
                                        BigDecimal.valueOf(55.0), BigDecimal.valueOf(275.0),
                                        BookingStatus.ACCEPTED, PaymentStatus.PAID);
                        bookingRepository.save(pastBooking1);

                        Booking pastBooking2 = new Booking(mealTable, renter2.getId(),
                                        LocalDate.now().minusDays(30), LocalDate.now().minusDays(25),
                                        BigDecimal.valueOf(150.0), BigDecimal.valueOf(750.0),
                                        BookingStatus.ACCEPTED, PaymentStatus.PAID);
                        bookingRepository.save(pastBooking2);

                        Booking pastBooking3 = new Booking(audioInterface, renter3.getId(),
                                        LocalDate.now().minusDays(10), LocalDate.now().minusDays(5),
                                        BigDecimal.valueOf(1200.0), BigDecimal.valueOf(6000.0),
                                        BookingStatus.ACCEPTED, PaymentStatus.PAID);
                        bookingRepository.save(pastBooking3);

                        Booking pastBooking4 = new Booking(speaker, renter1.getId(),
                                        LocalDate.now().minusDays(7), LocalDate.now().minusDays(3),
                                        BigDecimal.valueOf(40.0), BigDecimal.valueOf(160.0),
                                        BookingStatus.ACCEPTED, PaymentStatus.PAID);
                        bookingRepository.save(pastBooking4);

                        // Create upcoming bookings (future rentals)
                        Booking upcomingBooking1 = new Booking(partyLamps, renter2.getId(),
                                        LocalDate.now().plusDays(5), LocalDate.now().plusDays(10),
                                        BigDecimal.valueOf(55.0), BigDecimal.valueOf(275.0),
                                        BookingStatus.ACCEPTED, PaymentStatus.PAID);
                        bookingRepository.save(upcomingBooking1);

                        Booking upcomingBooking2 = new Booking(sunHat, renter3.getId(),
                                        LocalDate.now().plusDays(2), LocalDate.now().plusDays(5),
                                        BigDecimal.valueOf(80.0), BigDecimal.valueOf(240.0),
                                        BookingStatus.REQUESTED, PaymentStatus.PENDING);
                        bookingRepository.save(upcomingBooking2);

                        Booking upcomingBooking3 = new Booking(speaker, renter2.getId(),
                                        LocalDate.now().plusDays(15), LocalDate.now().plusDays(20),
                                        BigDecimal.valueOf(40.0), BigDecimal.valueOf(200.0),
                                        BookingStatus.ACCEPTED, PaymentStatus.PAID);
                        bookingRepository.save(upcomingBooking3);

                }
        }
}
