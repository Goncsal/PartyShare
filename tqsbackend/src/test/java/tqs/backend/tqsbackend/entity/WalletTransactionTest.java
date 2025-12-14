package tqs.backend.tqsbackend.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class WalletTransactionTest {

    private User owner;
    private Wallet wallet;
    private Item item;
    private Booking booking;
    private WalletTransaction transaction;

    @BeforeEach
    void setUp() {
        owner = new User("Owner", "owner@test.com", "password", UserRoles.OWNER);
        owner.setId(1L);
        
        wallet = new Wallet(owner);
        wallet.setId(1L);
        
        item = new Item();
        item.setId(1L);
        item.setOwnerId(1L);
        
        booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);
        booking.setRenterId(2L);
        booking.setTotalPrice(new BigDecimal("100.00"));
        
        transaction = new WalletTransaction(wallet, booking, new BigDecimal("100.00"));
    }

    @Test
    void testConstructor() {
        assertThat(transaction.getWallet()).isEqualTo(wallet);
        assertThat(transaction.getBooking()).isEqualTo(booking);
        assertThat(transaction.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
    }

    @Test
    void testRelease() {
        transaction.release();
        
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.RELEASED);
        assertThat(transaction.getReleasedAt()).isNotNull();
    }

    @Test
    void testRefund() {
        transaction.refund();
        
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.REFUNDED);
    }

    @Test
    void testSettersAndGetters() {
        transaction.setId(1L);
        transaction.setStatus(TransactionStatus.DISPUTED);
        
        assertThat(transaction.getId()).isEqualTo(1L);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.DISPUTED);
    }

    @Test
    void testOnCreate() {
        transaction.onCreate();
        assertThat(transaction.getCreatedAt()).isNotNull();
    }

    @Test
    void testTransactionStatusValues() {
        transaction.setStatus(TransactionStatus.PENDING);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        
        transaction.setStatus(TransactionStatus.RELEASED);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.RELEASED);
        
        transaction.setStatus(TransactionStatus.REFUNDED);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.REFUNDED);
        
        transaction.setStatus(TransactionStatus.DISPUTED);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.DISPUTED);
    }
}
