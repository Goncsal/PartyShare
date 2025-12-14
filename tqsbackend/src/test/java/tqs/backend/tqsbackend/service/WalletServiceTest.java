package tqs.backend.tqsbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tqs.backend.tqsbackend.entity.*;
import tqs.backend.tqsbackend.repository.BookingRepository;
import tqs.backend.tqsbackend.repository.WalletRepository;
import tqs.backend.tqsbackend.repository.WalletTransactionRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository transactionRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private WalletService walletService;

    private User owner;
    private Wallet wallet;
    private Item item;
    private Booking booking;

    @BeforeEach
    void setUp() {
        owner = new User("Owner", "owner@test.com", "pass", UserRoles.OWNER);
        owner.setId(1L);

        wallet = new Wallet(owner);
        wallet.setId(1L);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setPendingBalance(BigDecimal.ZERO);

        item = new Item();
        item.setId(1L);
        item.setOwnerId(1L);

        booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);
        booking.setRenterId(2L);
        booking.setTotalPrice(new BigDecimal("100.00"));
    }

    @Test
    void createWallet_Success() {
        when(walletRepository.existsByOwnerId(1L)).thenReturn(false);
        when(userService.getUserById(1L)).thenReturn(Optional.of(owner));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        Wallet result = walletService.createWallet(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void createWallet_AlreadyExists_Throws() {
        when(walletRepository.existsByOwnerId(1L)).thenReturn(true);

        assertThatThrownBy(() -> walletService.createWallet(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createWallet_UserNotOwner_Throws() {
        User renter = new User("Renter", "renter@test.com", "pass", UserRoles.RENTER);
        renter.setId(2L);

        when(walletRepository.existsByOwnerId(2L)).thenReturn(false);
        when(userService.getUserById(2L)).thenReturn(Optional.of(renter));

        assertThatThrownBy(() -> walletService.createWallet(2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only OWNER");
    }

    @Test
    void holdFunds_Success() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(walletRepository.findByOwnerId(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(transactionRepository.save(any(WalletTransaction.class))).thenAnswer(inv -> {
            WalletTransaction tx = inv.getArgument(0);
            tx.setId(1L);
            return tx;
        });

        WalletTransaction result = walletService.holdFunds(1L);

        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(wallet.getPendingBalance()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void holdFunds_BookingNotFound_Throws() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.holdFunds(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Booking not found");
    }

    @Test
    void releaseFunds_Success() {
        wallet.setPendingBalance(new BigDecimal("100.00"));
        booking.setRenterConfirmed(true);
        booking.setOwnerConfirmed(true);

        WalletTransaction transaction = new WalletTransaction(wallet, booking, new BigDecimal("100.00"));
        transaction.setId(1L);

        when(transactionRepository.findByBookingId(1L)).thenReturn(Optional.of(transaction));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(transactionRepository.save(any(WalletTransaction.class))).thenReturn(transaction);

        boolean result = walletService.releaseFunds(1L);

        assertThat(result).isTrue();
        assertThat(wallet.getBalance()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(wallet.getPendingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.RELEASED);
    }

    @Test
    void releaseFunds_NotDualConfirmed_ReturnsFalse() {
        booking.setRenterConfirmed(true);
        booking.setOwnerConfirmed(false);

        WalletTransaction transaction = new WalletTransaction(wallet, booking, new BigDecimal("100.00"));
        transaction.setId(1L);

        when(transactionRepository.findByBookingId(1L)).thenReturn(Optional.of(transaction));

        boolean result = walletService.releaseFunds(1L);

        assertThat(result).isFalse();
    }

    @Test
    void refundFunds_Success() {
        wallet.setPendingBalance(new BigDecimal("100.00"));

        WalletTransaction transaction = new WalletTransaction(wallet, booking, new BigDecimal("100.00"));
        transaction.setId(1L);

        when(transactionRepository.findByBookingId(1L)).thenReturn(Optional.of(transaction));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(transactionRepository.save(any(WalletTransaction.class))).thenReturn(transaction);

        boolean result = walletService.refundFunds(1L);

        assertThat(result).isTrue();
        assertThat(wallet.getPendingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.REFUNDED);
    }

    @Test
    void withdraw_Success() {
        wallet.setBalance(new BigDecimal("500.00"));
        when(walletRepository.findByOwnerId(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        BigDecimal newBalance = walletService.withdraw(1L, new BigDecimal("200.00"));

        assertThat(newBalance).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(wallet.getBalance()).isEqualByComparingTo(new BigDecimal("300.00"));
    }

    @Test
    void withdraw_InsufficientBalance_Throws() {
        wallet.setBalance(new BigDecimal("50.00"));
        when(walletRepository.findByOwnerId(1L)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> walletService.withdraw(1L, new BigDecimal("100.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient balance");
    }

    @Test
    void withdrawAll_Success() {
        wallet.setBalance(new BigDecimal("500.00"));
        when(walletRepository.findByOwnerId(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        BigDecimal withdrawn = walletService.withdrawAll(1L);

        assertThat(withdrawn).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(wallet.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void withdrawAll_NoBalance_Throws() {
        wallet.setBalance(BigDecimal.ZERO);
        when(walletRepository.findByOwnerId(1L)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> walletService.withdrawAll(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No available balance");
    }

    @Test
    void getWalletByOwnerId_ReturnsWallet() {
        when(walletRepository.findByOwnerId(1L)).thenReturn(Optional.of(wallet));

        Optional<Wallet> result = walletService.getWalletByOwnerId(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void getWalletByOwnerId_NotFound_ReturnsEmpty() {
        when(walletRepository.findByOwnerId(99L)).thenReturn(Optional.empty());

        Optional<Wallet> result = walletService.getWalletByOwnerId(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void getTransactionsByOwnerId_ReturnsTransactions() {
        WalletTransaction tx = new WalletTransaction(wallet, booking, new BigDecimal("100.00"));
        tx.setId(1L);

        when(walletRepository.findByOwnerId(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByWalletId(1L)).thenReturn(List.of(tx));

        List<WalletTransaction> result = walletService.getTransactionsByOwnerId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void getTransactionsByOwnerId_NoWallet_ReturnsEmpty() {
        when(walletRepository.findByOwnerId(99L)).thenReturn(Optional.empty());

        List<WalletTransaction> result = walletService.getTransactionsByOwnerId(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void createWallet_UserNotFound_Throws() {
        when(walletRepository.existsByOwnerId(99L)).thenReturn(false);
        when(userService.getUserById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.createWallet(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void holdFunds_CreatesWalletIfNotExists() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(walletRepository.findByOwnerId(1L)).thenReturn(Optional.empty());
        when(userService.getUserById(1L)).thenReturn(Optional.of(owner));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(transactionRepository.save(any(WalletTransaction.class))).thenAnswer(inv -> {
            WalletTransaction tx = inv.getArgument(0);
            tx.setId(1L);
            return tx;
        });

        WalletTransaction result = walletService.holdFunds(1L);

        assertThat(result).isNotNull();
        verify(walletRepository, times(2)).save(any(Wallet.class)); // Once for create, once for update
    }

    @Test
    void releaseFunds_TransactionNotFound_ReturnsFalse() {
        when(transactionRepository.findByBookingId(99L)).thenReturn(Optional.empty());

        boolean result = walletService.releaseFunds(99L);

        assertThat(result).isFalse();
    }

    @Test
    void releaseFunds_TransactionNotPending_ReturnsFalse() {
        WalletTransaction transaction = new WalletTransaction(wallet, booking, new BigDecimal("100.00"));
        transaction.setId(1L);
        transaction.release(); // Already released

        when(transactionRepository.findByBookingId(1L)).thenReturn(Optional.of(transaction));

        boolean result = walletService.releaseFunds(1L);

        assertThat(result).isFalse();
    }

    @Test
    void refundFunds_TransactionNotFound_ReturnsFalse() {
        when(transactionRepository.findByBookingId(99L)).thenReturn(Optional.empty());

        boolean result = walletService.refundFunds(99L);

        assertThat(result).isFalse();
    }

    @Test
    void refundFunds_TransactionNotPending_ReturnsFalse() {
        WalletTransaction transaction = new WalletTransaction(wallet, booking, new BigDecimal("100.00"));
        transaction.setId(1L);
        transaction.refund(); // Already refunded

        when(transactionRepository.findByBookingId(1L)).thenReturn(Optional.of(transaction));

        boolean result = walletService.refundFunds(1L);

        assertThat(result).isFalse();
    }

    @Test
    void withdraw_WalletNotFound_Throws() {
        when(walletRepository.findByOwnerId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.withdraw(99L, new BigDecimal("100.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Wallet not found");
    }

    @Test
    void withdraw_InvalidAmount_Throws() {
        when(walletRepository.findByOwnerId(1L)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> walletService.withdraw(1L, new BigDecimal("-10.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
    }

    @Test
    void withdrawAll_WalletNotFound_Throws() {
        when(walletRepository.findByOwnerId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.withdrawAll(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Wallet not found");
    }
}
