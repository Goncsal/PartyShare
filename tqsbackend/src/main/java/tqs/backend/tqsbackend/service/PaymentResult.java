package tqs.backend.tqsbackend.service;

public record PaymentResult(boolean success, String reference, String reason) {

    public static PaymentResult success(String reference) {
        return new PaymentResult(true, reference, null);
    }

    public static PaymentResult failure(String reason) {
        return new PaymentResult(false, null, reason);
    }
}
