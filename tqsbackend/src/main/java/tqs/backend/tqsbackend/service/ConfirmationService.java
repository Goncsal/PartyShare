package tqs.backend.tqsbackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tqs.backend.tqsbackend.entity.Booking;
import tqs.backend.tqsbackend.repository.BookingRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ConfirmationService {

    private static final Logger logger = LoggerFactory.getLogger(ConfirmationService.class);

    private final BookingRepository bookingRepository;
    private final WalletService walletService;

    public ConfirmationService(BookingRepository bookingRepository, WalletService walletService) {
        this.bookingRepository = bookingRepository;
        this.walletService = walletService;
    }

    @Transactional
    public boolean confirmByRenter(Long bookingId, Long renterId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            logger.warn("Cannot confirm: Booking {} not found", bookingId);
            return false;
        }

        Booking booking = bookingOpt.get();
        if (!booking.getRenterId().equals(renterId)) {
            logger.warn("Cannot confirm: User {} is not the renter of booking {}", renterId, bookingId);
            return false;
        }

        if (booking.isRenterConfirmed()) {
            logger.info("Booking {} already confirmed by renter", bookingId);
            return true;
        }

        booking.setRenterConfirmed(true);
        if (booking.getReturnedAt() == null) {
            booking.setReturnedAt(LocalDateTime.now());
        }
        bookingRepository.save(booking);

        logger.info("Renter {} confirmed booking {}", renterId, bookingId);
        return checkAndRelease(booking);
    }

    @Transactional
    public boolean confirmByOwner(Long bookingId, Long ownerId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            logger.warn("Cannot confirm: Booking {} not found", bookingId);
            return false;
        }

        Booking booking = bookingOpt.get();
        if (!booking.getItem().getOwnerId().equals(ownerId)) {
            logger.warn("Cannot confirm: User {} is not the owner of booking {}", ownerId, bookingId);
            return false;
        }

        if (booking.isOwnerConfirmed()) {
            logger.info("Booking {} already confirmed by owner", bookingId);
            return true;
        }

        booking.setOwnerConfirmed(true);
        bookingRepository.save(booking);

        logger.info("Owner {} confirmed booking {}", ownerId, bookingId);
        return checkAndRelease(booking);
    }

    private boolean checkAndRelease(Booking booking) {
        if (booking.isRenterConfirmed() && booking.isOwnerConfirmed()) {
            logger.info("Both parties confirmed for booking {}. Releasing funds.", booking.getId());
            return walletService.releaseFunds(booking.getId());
        }
        return true;
    }

    public boolean isFullyConfirmed(Long bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return false;
        }
        Booking booking = bookingOpt.get();
        return booking.isRenterConfirmed() && booking.isOwnerConfirmed();
    }
}
