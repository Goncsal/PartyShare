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

        BigDecimal dailyPrice;
        if (request.getProposedPrice() != null) {
            validatePrice(request.getProposedPrice());
            dailyPrice = BigDecimal.valueOf(request.getProposedPrice());
        } else {
            dailyPrice = BigDecimal.valueOf(item.getPrice());
        }
        
        BigDecimal totalPrice = calculateTotalPrice(dailyPrice, request.getStartDate(), request.getEndDate());

        Booking booking = new Booking(item, request.getRenterId(), request.getStartDate(), request.getEndDate(),
                dailyPrice, totalPrice, BookingStatus.REQUESTED, PaymentStatus.PENDING);

        try {
            booking = bookingRepository.save(booking);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return booking;
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
                itemId, Arrays.asList(BookingStatus.ACCEPTED, BookingStatus.REQUESTED), endDate, startDate);
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

        if (booking.getStatus() != BookingStatus.REQUESTED && booking.getStatus() != BookingStatus.ACCEPTED) {
            throw new BookingValidationException("This booking cannot be cancelled");
        }

        if (!booking.getEndDate().isAfter(LocalDate.now())) {
            throw new BookingValidationException("Booking has already ended");
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

    public List<Booking> getPendingBookingsByOwner(Long ownerId) {
        return bookingRepository.findByItem_OwnerIdAndStatus(ownerId, BookingStatus.REQUESTED);
    }

    @Transactional
    public Booking acceptBooking(Long bookingId, Long ownerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (!booking.getItem().getOwnerId().equals(ownerId)) {
            throw new BookingValidationException("You can only accept bookings for your own items");
        }
        
        booking.setStatus(BookingStatus.ACCEPTED);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking declineBooking(Long bookingId, Long ownerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (!booking.getItem().getOwnerId().equals(ownerId)) {
            throw new BookingValidationException("You can only decline bookings for your own items");
        }
        
        booking.setStatus(BookingStatus.REJECTED);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking counterOfferBooking(Long bookingId, Double newPrice, Long ownerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (!booking.getItem().getOwnerId().equals(ownerId)) {
            throw new BookingValidationException("You can only make counter-offers for your own items");
        }
        
        booking.setStatus(BookingStatus.COUNTER_OFFER);
        BigDecimal daily = BigDecimal.valueOf(newPrice);
        booking.setDailyPrice(daily);
        booking.setTotalPrice(calculateTotalPrice(daily, booking.getStartDate(), booking.getEndDate()));
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking acceptCounterOffer(Long bookingId, Long renterId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (!booking.getRenterId().equals(renterId)) {
            throw new BookingValidationException("You can only accept counter-offers for your own bookings");
        }
        
        booking.setStatus(BookingStatus.ACCEPTED);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking declineCounterOffer(Long bookingId, Long renterId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getRenterId().equals(renterId)) {
            throw new BookingValidationException("You can only decline counter-offers for your own bookings");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }
}
