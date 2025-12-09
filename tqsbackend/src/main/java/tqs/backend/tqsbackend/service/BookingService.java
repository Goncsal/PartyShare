package tqs.backend.tqsbackend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tqs.backend.tqsbackend.dto.BookingCreateRequest;
import tqs.backend.tqsbackend.entity.Booking;
import tqs.backend.tqsbackend.entity.BookingStatus;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.entity.PaymentStatus;
import tqs.backend.tqsbackend.exception.AvailabilityException;
import tqs.backend.tqsbackend.exception.BookingValidationException;
import tqs.backend.tqsbackend.exception.PaymentException;
import tqs.backend.tqsbackend.repository.BookingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;

    private final ItemService itemService;

    private final PaymentService paymentService;

    @Transactional
    public Booking createBooking(BookingCreateRequest request) {
        Item item = itemService.getItemById(request.getItemId());
        if (item == null) {
            throw new BookingValidationException("Item not found");
        }

        validateRenter(request.getRenterId(), item);
        validatePrice(item.getPrice());
        validateDates(request.getStartDate(), request.getEndDate());
        ensureAvailability(item.getId(), request.getStartDate(), request.getEndDate());

        BigDecimal dailyPrice = BigDecimal.valueOf(item.getPrice());
        BigDecimal totalPrice = calculateTotalPrice(dailyPrice, request.getStartDate(), request.getEndDate());

        Booking booking = new Booking(item, request.getRenterId(), request.getStartDate(), request.getEndDate(),
                dailyPrice, totalPrice, BookingStatus.PENDING, PaymentStatus.PENDING);

        booking = bookingRepository.save(booking);

        PaymentResult paymentResult = paymentService.charge(request.getRenterId(), item, totalPrice, booking.getId());
        if (paymentResult.success()) {
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setPaymentStatus(PaymentStatus.PAID);
            booking.setPaymentReference(paymentResult.reference());
            return bookingRepository.save(booking);
        }

        booking.setStatus(BookingStatus.REJECTED);
        booking.setPaymentStatus(PaymentStatus.FAILED);
        booking.setPaymentReference(paymentResult.reference());
        bookingRepository.save(booking);
        throw new PaymentException(paymentResult.reason() != null ? paymentResult.reason() : "Payment failed");
    }

    public Booking getBooking(Long id) {
        return bookingRepository.findById(id).orElse(null);
    }

    public List<Booking> getBookingsForRenter(Long renterId) {
        return bookingRepository.findByRenterId(renterId);
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BookingValidationException("Start and end dates are required");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new BookingValidationException("Start date cannot be in the past");
        }
        if (!endDate.isAfter(startDate)) {
            throw new BookingValidationException("End date must be after start date");
        }
    }

    private void validateRenter(Long renterId, Item item) {
        if (renterId == null) {
            throw new BookingValidationException("Renter is required");
        }
        if (item.getOwnerId() != null && item.getOwnerId().equals(renterId)) {
            throw new BookingValidationException("Cannot book your own item");
        }
    }

    private void ensureAvailability(Long itemId, LocalDate startDate, LocalDate endDate) {
        boolean overlapping = bookingRepository.existsByItemIdAndStatusInAndStartDateLessThanAndEndDateGreaterThan(
                itemId, Arrays.asList(BookingStatus.CONFIRMED, BookingStatus.PENDING), endDate, startDate);
        if (overlapping) {
            throw new AvailabilityException("Item not available for the selected dates");
        }
    }

    private void validatePrice(Double price) {
        if (price == null || price <= 0) {
            throw new BookingValidationException("Item price must be greater than zero");
        }
    }

    private BigDecimal calculateTotalPrice(BigDecimal dailyPrice, LocalDate startDate, LocalDate endDate) {
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days <= 0) {
            throw new BookingValidationException("Booking must be at least one day");
        }
        return dailyPrice.multiply(BigDecimal.valueOf(days));
    }

    @Transactional
    public Booking cancelBooking(Long bookingId, Long renterId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingValidationException("Booking not found"));

        if (!booking.getRenterId().equals(renterId)) {
            throw new BookingValidationException("You can only cancel your own bookings");
        }

        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BookingValidationException("This booking cannot be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    public List<tqs.backend.tqsbackend.dto.DateRangeDto> getUnavailableDates(Long itemId) {
        return bookingRepository.findActiveBookingsByItemId(itemId)
                .stream()
                .map(b -> new tqs.backend.tqsbackend.dto.DateRangeDto(b.getStartDate(), b.getEndDate()))
                .toList();
    }
}
