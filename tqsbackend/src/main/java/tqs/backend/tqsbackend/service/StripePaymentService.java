package tqs.backend.tqsbackend.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

import tqs.backend.tqsbackend.entity.Item;

@Service
@Primary
@Profile("stripe")
public class StripePaymentService implements PaymentService {

    @Value("${stripe.api.secret-key}")
    private String secretKey;

    @Override
    public PaymentResult charge(Long renterId, Item item, BigDecimal amount, Long bookingId) {
        try {
            Stripe.apiKey = secretKey;

            Map<String, Object> params = new HashMap<>();
            // Stripe expects amount in cents
            params.put("amount", amount.multiply(BigDecimal.valueOf(100)).longValue());
            params.put("currency", "eur");
            params.put("description", "Booking #" + bookingId + " - " + item.getName());
            params.put("automatic_payment_methods", Map.of(
                "enabled", true,
                "allow_redirects", "never"
            ));
            
            Map<String, String> metadata = new HashMap<>();
            metadata.put("booking_id", bookingId.toString());
            metadata.put("renter_id", renterId.toString());
            metadata.put("item_id", item.getId().toString());
            metadata.put("item_name", item.getName());
            params.put("metadata", metadata);

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            
            // For sandbox testing, we'll confirm immediately with a test payment method
            Map<String, Object> confirmParams = new HashMap<>();
            confirmParams.put("payment_method", "pm_card_visa");
            paymentIntent = paymentIntent.confirm(confirmParams);

            if ("succeeded".equals(paymentIntent.getStatus())) {
                return PaymentResult.success(paymentIntent.getId());
            } else if ("requires_action".equals(paymentIntent.getStatus())) {
                // Payment requires additional authentication
                return PaymentResult.success(paymentIntent.getId());
            } else {
                return PaymentResult.failure("Payment status: " + paymentIntent.getStatus());
            }

        } catch (StripeException e) {
            return PaymentResult.failure(e.getMessage());
        }
    }
}
