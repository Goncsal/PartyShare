package tqs.backend.tqsbackend.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class WalletTest {

    private User owner;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        owner = new User("Owner", "owner@test.com", "password", UserRoles.OWNER);
        owner.setId(1L);
        wallet = new Wallet(owner);
    }

    @Test
    void testConstructor() {
        assertThat(wallet.getOwner()).isEqualTo(owner);
        assertThat(wallet.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(wallet.getPendingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void testAddPendingFunds() {
        wallet.addPendingFunds(new BigDecimal("100.00"));
        assertThat(wallet.getPendingBalance()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(wallet.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);

        wallet.addPendingFunds(new BigDecimal("50.00"));
        assertThat(wallet.getPendingBalance()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    void testReleasePendingFunds() {
        wallet.addPendingFunds(new BigDecimal("100.00"));
        wallet.releasePendingFunds(new BigDecimal("60.00"));

        assertThat(wallet.getPendingBalance()).isEqualByComparingTo(new BigDecimal("40.00"));
        assertThat(wallet.getBalance()).isEqualByComparingTo(new BigDecimal("60.00"));
    }

    @Test
    void testRefundPendingFunds() {
        wallet.addPendingFunds(new BigDecimal("100.00"));
        wallet.refundPendingFunds(new BigDecimal("100.00"));

        assertThat(wallet.getPendingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(wallet.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void testSettersAndGetters() {
        wallet.setId(1L);
        wallet.setBalance(new BigDecimal("500.00"));
        wallet.setPendingBalance(new BigDecimal("100.00"));

        assertThat(wallet.getId()).isEqualTo(1L);
        assertThat(wallet.getBalance()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(wallet.getPendingBalance()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void testOnCreate() {
        wallet.onCreate();
        assertThat(wallet.getCreatedAt()).isNotNull();
        assertThat(wallet.getUpdatedAt()).isNotNull();
        assertThat(wallet.getCreatedAt()).isEqualTo(wallet.getUpdatedAt());
    }

    @Test
    void testOnUpdate() {
        wallet.onCreate();
        var createdAt = wallet.getCreatedAt();
        
        wallet.onUpdate();
        
        assertThat(wallet.getCreatedAt()).isEqualTo(createdAt);
        assertThat(wallet.getUpdatedAt()).isNotNull();
    }
}
