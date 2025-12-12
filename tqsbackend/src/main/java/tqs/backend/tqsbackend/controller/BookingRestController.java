package tqs.backend.tqsbackend.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tqs.backend.tqsbackend.dto.BookingCreateRequest;
import tqs.backend.tqsbackend.dto.BookingResponse;
import tqs.backend.tqsbackend.entity.Booking;
import tqs.backend.tqsbackend.service.BookingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingRestController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody @Valid BookingCreateRequest request) {
        Booking booking = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(booking));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable Long id) {
        Booking booking = bookingService.getBooking(id);
        if (booking == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toResponse(booking));
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getItem().getId(),
                booking.getRenterId(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getTotalPrice(),
                booking.getStatus(),
                booking.getPaymentStatus(),
                booking.getPaymentReference());
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long id,
            @RequestParam Long renterId) {
        Booking booking = bookingService.cancelBooking(id, renterId);
        return ResponseEntity.ok(toResponse(booking));
    }

    @GetMapping("/unavailable/{itemId}")
    public ResponseEntity<java.util.List<tqs.backend.tqsbackend.dto.DateRangeDto>> getUnavailableDates(
            @PathVariable Long itemId) {
        return ResponseEntity.ok(bookingService.getUnavailableDates(itemId));
    }

    @GetMapping("/requests")
    public ResponseEntity<java.util.List<BookingResponse>> getBookingRequests(@RequestParam Long ownerId) {
        return ResponseEntity.ok(bookingService.getPendingBookingsByOwner(ownerId).stream()
                .map(this::toResponse)
                .toList());
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<BookingResponse> acceptBooking(@PathVariable Long id, @RequestParam Long ownerId) {
        Booking booking = bookingService.acceptBooking(id, ownerId);
        return ResponseEntity.ok(toResponse(booking));
    }

    @PostMapping("/{id}/decline")
    public ResponseEntity<BookingResponse> declineBooking(@PathVariable Long id, @RequestParam Long ownerId) {
        Booking booking = bookingService.declineBooking(id, ownerId);
        return ResponseEntity.ok(toResponse(booking));
    }

    @PostMapping("/{id}/counter-offer")
    public ResponseEntity<BookingResponse> counterOfferBooking(
            @PathVariable Long id,
            @RequestParam Double newPrice,
            @RequestParam Long ownerId) {
        Booking booking = bookingService.counterOfferBooking(id, newPrice, ownerId);
        return ResponseEntity.ok(toResponse(booking));
    }

    @PostMapping("/{id}/accept-counter-offer")
    public ResponseEntity<BookingResponse> acceptCounterOffer(@PathVariable Long id, @RequestParam Long renterId) {
        Booking booking = bookingService.acceptCounterOffer(id, renterId);
        return ResponseEntity.ok(toResponse(booking));
    }

    @PostMapping("/{id}/decline-counter-offer")
    public ResponseEntity<BookingResponse> declineCounterOffer(@PathVariable Long id, @RequestParam Long renterId) {
        Booking booking = bookingService.declineCounterOffer(id, renterId);
        return ResponseEntity.ok(toResponse(booking));
    }
}
