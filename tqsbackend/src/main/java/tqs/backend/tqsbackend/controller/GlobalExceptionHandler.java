package tqs.backend.tqsbackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tqs.backend.tqsbackend.exception.AvailabilityException;
import tqs.backend.tqsbackend.exception.BookingValidationException;
import tqs.backend.tqsbackend.exception.PaymentException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookingValidationException.class)
    public ResponseEntity<String> handleValidation(BookingValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(AvailabilityException.class)
    public ResponseEntity<String> handleAvailability(AvailabilityException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<String> handlePayment(PaymentException ex) {
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleBeanValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Invalid request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
