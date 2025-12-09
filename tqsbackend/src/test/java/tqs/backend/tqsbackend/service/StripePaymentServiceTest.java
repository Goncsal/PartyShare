package tqs.backend.tqsbackend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tqs.backend.tqsbackend.entity.Category;
import tqs.backend.tqsbackend.entity.Item;

class StripePaymentServiceTest {

    private StripePaymentService stripePaymentService;

    @BeforeEach
    void setUp() {
        stripePaymentService = new StripePaymentService();
    }

    private Item createTestItem() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Test");

        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setPrice(10.0);
        item.setCategory(category);
        return item;
    }

    @Test
    void charge_WithNullAmount_ThrowsException() {
        Item item = createTestItem();
        
        // Amount is null - should fail
        assertThatThrownBy(() -> 
            stripePaymentService.charge(1L, item, null, 1L)
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void charge_WithEmptyApiKey_ReturnsFailure() {
        Item item = createTestItem();
        BigDecimal amount = BigDecimal.valueOf(20.0);

        // API key is not set (empty), so Stripe call will fail
        PaymentResult result = stripePaymentService.charge(1L, item, amount, 1L);
        
        // Should return failure due to missing API key
        assertThat(result.success()).isFalse();
    }

    @Test
    void charge_WithValidData_ButNoApiKey_ReturnsFailure() {
        Item item = createTestItem();
        BigDecimal amount = BigDecimal.valueOf(50.0);

        PaymentResult result = stripePaymentService.charge(1L, item, amount, 2L);
        
        assertThat(result).isNotNull();
        assertThat(result.success()).isFalse();
        assertThat(result.reason()).isNotEmpty();
    }
}
