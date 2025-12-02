package tqs.backend.tqsbackend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import tqs.backend.tqsbackend.entity.Item;

class MockPaymentServiceTest {

    private final MockPaymentService paymentService = new MockPaymentService();

    @Test
    void charge_returnsSuccessfulPaymentWithReference() {
        Item item = new Item();
        item.setId(15L);
        item.setPrice(25.0);

        PaymentResult result = paymentService.charge(70L, item, BigDecimal.valueOf(50), 88L);

        assertThat(result.success()).isTrue();
        assertThat(result.reference()).isNotNull();
        assertThat(result.reference()).contains("PAY-88");
        assertThat(result.reason()).isNull();
    }
}
