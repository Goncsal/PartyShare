package tqs.backend.tqsbackend.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import tqs.backend.tqsbackend.entity.Booking;
import tqs.backend.tqsbackend.service.BookingService;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final BookingService bookingService;

    @Value("${stripe.api.secret-key:}")
    private String stripeSecretKey;

    @Value("${stripe.api.public-key:}")
    private String stripePublicKey;

    @GetMapping("/{bookingId}")
    public String showPaymentPage(@PathVariable Long bookingId, Model model) {
        Booking booking = bookingService.getBooking(bookingId);
        if (booking == null) {
            return "redirect:/bookings?error=Booking not found";
        }

        model.addAttribute("booking", booking);
        model.addAttribute("stripePublicKey", stripePublicKey);
        return "payment/checkout";
    }

    @PostMapping("/create-checkout-session/{bookingId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> createCheckoutSession(
            @PathVariable Long bookingId,
            HttpServletRequest request) {
        
        try {
            Stripe.apiKey = stripeSecretKey;
            
            Booking booking = bookingService.getBooking(bookingId);
            if (booking == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Booking not found"));
            }

            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();

            SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(baseUrl + "/payment/success/" + bookingId + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(baseUrl + "/payment/cancel/" + bookingId)
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("eur")
                                .setUnitAmount(booking.getTotalPrice().multiply(BigDecimal.valueOf(100)).longValue())
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(booking.getItem().getName())
                                        .setDescription("Rental from " + booking.getStartDate() + " to " + booking.getEndDate())
                                        .build())
                                .build())
                        .build())
                .putMetadata("booking_id", bookingId.toString())
                .build();

            Session session = Session.create(params);

            Map<String, String> response = new HashMap<>();
            response.put("url", session.getUrl());
            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/success/{bookingId}")
    public String paymentSuccess(@PathVariable Long bookingId, Model model) {
        Booking booking = bookingService.getBooking(bookingId);
        model.addAttribute("booking", booking);
        return "payment/success";
    }

    @GetMapping("/cancel/{bookingId}")
    public String paymentCancel(@PathVariable Long bookingId, Model model) {
        Booking booking = bookingService.getBooking(bookingId);
        model.addAttribute("booking", booking);
        return "payment/cancel";
    }
}
