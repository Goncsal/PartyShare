package tqs.backend.tqsbackend.service;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;
import tqs.backend.tqsbackend.entity.Item;

@Service
public class MockPaymentService implements PaymentService {

    @Override
    public PaymentResult charge(Long renterId, Item item, BigDecimal amount, Long bookingId) {
        String reference = "PAY-" + bookingId + "-" + UUID.randomUUID();
        return PaymentResult.success(reference);
    }
}
