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
import tqs.backend.tqsbackend.entity.Wallet;
import tqs.backend.tqsbackend.entity.WalletTransaction;
import tqs.backend.tqsbackend.repository.BookingRepository;
import tqs.backend.tqsbackend.repository.CategoryRepository;
import tqs.backend.tqsbackend.repository.ItemRepository;
import tqs.backend.tqsbackend.repository.UserRepository;
import tqs.backend.tqsbackend.repository.WalletRepository;
import tqs.backend.tqsbackend.repository.WalletTransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class MockAddingItemService {

        private final ItemRepository itemRepository;
        private final CategoryRepository categoryRepository;
        private final UserRepository userRepository;
        private final BookingRepository bookingRepository;
        private final WalletRepository walletRepository;
        private final WalletTransactionRepository walletTransactionRepository;

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

                        // Create wallets for owners
                        Wallet wallet1 = new Wallet(owner1);
                        Wallet wallet2 = new Wallet(owner2);
                        Wallet wallet3 = new Wallet(owner3);
                        walletRepository.saveAll(Arrays.asList(wallet1, wallet2, wallet3));

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

                        // Create past bookings with dual confirmation (completed & released)
                        Booking pastBooking1 = new Booking(partyLamps, renter1.getId(),
                                        LocalDate.now().minusDays(20), LocalDate.now().minusDays(15),
                                        BigDecimal.valueOf(55.0), BigDecimal.valueOf(275.0),
                                        BookingStatus.ACCEPTED, PaymentStatus.PAID);
                        pastBooking1.setRenterConfirmed(true);
                        pastBooking1.setOwnerConfirmed(true);
                        pastBooking1.setReturnedAt(LocalDateTime.now().minusDays(15));
                        bookingRepository.save(pastBooking1);

                        // Released transaction - funds in owner's available balance
                        WalletTransaction tx1 = new WalletTransaction(wallet1, pastBooking1, BigDecimal.valueOf(275.0));
                        tx1.release();
                        walletTransactionRepository.save(tx1);
                        wallet1.setBalance(wallet1.getBalance().add(BigDecimal.valueOf(275.0)));
                        walletRepository.save(wallet1);

                        Booking pastBooking2 = new Booking(mealTable, renter2.getId(),
                                        LocalDate.now().minusDays(30), LocalDate.now().minusDays(25),
                                        BigDecimal.valueOf(150.0), BigDecimal.valueOf(750.0),
                                        BookingStatus.ACCEPTED, PaymentStatus.PAID);
                        pastBooking2.setRenterConfirmed(true);
                        pastBooking2.setOwnerConfirmed(true);
                        pastBooking2.setReturnedAt(LocalDateTime.now().minusDays(25));
                        bookingRepository.save(pastBooking2);

                        // Released transaction
                        WalletTransaction tx2 = new WalletTransaction(wallet1, pastBooking2, BigDecimal.valueOf(750.0));
                        tx2.release();
                        walletTransactionRepository.save(tx2);
                        wallet1.setBalance(wallet1.getBalance().add(BigDecimal.valueOf(750.0)));
                        walletRepository.save(wallet1);

                        // Past booking awaiting owner confirmation only
                        Booking pastBooking3 = new Booking(audioInterface, renter3.getId(),
                                        LocalDate.now().minusDays(10), LocalDate.now().minusDays(5),
                                        BigDecimal.valueOf(1200.0), BigDecimal.valueOf(6000.0),
                                        BookingStatus.ACCEPTED, PaymentStatus.PAID);
                        pastBooking3.setRenterConfirmed(true);
                        pastBooking3.setOwnerConfirmed(false); // Owner hasn't confirmed yet
                        bookingRepository.save(pastBooking3);

                        // Pending transaction - funds held in owner's pending balance
                        WalletTransaction tx3 = new WalletTransaction(wallet2, pastBooking3,
                                        BigDecimal.valueOf(6000.0));
                        walletTransactionRepository.save(tx3);
                        wallet2.setPendingBalance(wallet2.getPendingBalance().add(BigDecimal.valueOf(6000.0)));
                        walletRepository.save(wallet2);

                        // Past booking with no confirmations yet
                        Booking pastBooking4 = new Booking(speaker, renter1.getId(),
                                        LocalDate.now().minusDays(7), LocalDate.now().minusDays(3),
                                        BigDecimal.valueOf(40.0), BigDecimal.valueOf(160.0),
                                        BookingStatus.ACCEPTED, PaymentStatus.PAID);
                        pastBooking4.setRenterConfirmed(false);
                        pastBooking4.setOwnerConfirmed(false);
                        bookingRepository.save(pastBooking4);

                        // Pending transaction
                        WalletTransaction tx4 = new WalletTransaction(wallet3, pastBooking4, BigDecimal.valueOf(160.0));
                        walletTransactionRepository.save(tx4);
                        wallet3.setPendingBalance(wallet3.getPendingBalance().add(BigDecimal.valueOf(160.0)));
                        walletRepository.save(wallet3);

                        // Create upcoming bookings (future rentals)
                        Booking upcomingBooking1 = new Booking(partyLamps, renter2.getId(),
                                        LocalDate.now().plusDays(5), LocalDate.now().plusDays(10),
                                        BigDecimal.valueOf(55.0), BigDecimal.valueOf(275.0),
                                        BookingStatus.ACCEPTED, PaymentStatus.PAID);
                        bookingRepository.save(upcomingBooking1);

                        // Pending transaction for upcoming rental
                        WalletTransaction tx5 = new WalletTransaction(wallet1, upcomingBooking1,
                                        BigDecimal.valueOf(275.0));
                        walletTransactionRepository.save(tx5);
                        wallet1.setPendingBalance(wallet1.getPendingBalance().add(BigDecimal.valueOf(275.0)));
                        walletRepository.save(wallet1);

                        Booking upcomingBooking2 = new Booking(sunHat, renter3.getId(),
                                        LocalDate.now().plusDays(2), LocalDate.now().plusDays(5),
                                        BigDecimal.valueOf(80.0), BigDecimal.valueOf(240.0),
                                        BookingStatus.REQUESTED, PaymentStatus.PENDING);
                        bookingRepository.save(upcomingBooking2);
                        // No wallet transaction for PENDING payments

                        Booking upcomingBooking3 = new Booking(speaker, renter2.getId(),
                                        LocalDate.now().plusDays(15), LocalDate.now().plusDays(20),
                                        BigDecimal.valueOf(40.0), BigDecimal.valueOf(200.0),
                                        BookingStatus.ACCEPTED, PaymentStatus.PAID);
                        bookingRepository.save(upcomingBooking3);

                        // Pending transaction
                        WalletTransaction tx6 = new WalletTransaction(wallet3, upcomingBooking3,
                                        BigDecimal.valueOf(200.0));
                        walletTransactionRepository.save(tx6);
                        wallet3.setPendingBalance(wallet3.getPendingBalance().add(BigDecimal.valueOf(200.0)));
                        walletRepository.save(wallet3);
                }
        }
}
