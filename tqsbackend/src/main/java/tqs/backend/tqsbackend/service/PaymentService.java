package tqs.backend.tqsbackend.service;

import java.math.BigDecimal;
import tqs.backend.tqsbackend.entity.Item;

public interface PaymentService {

    PaymentResult charge(Long renterId, Item item, BigDecimal amount, Long bookingId);
}
