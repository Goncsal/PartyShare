package tqs.backend.tqsbackend.service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tqs.backend.tqsbackend.entity.BookingStatus;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.entity.Rating;
import tqs.backend.tqsbackend.entity.RatingType;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.repository.BookingRepository;
import tqs.backend.tqsbackend.repository.RatingRepository;

@Service
public class RatingService {
    private static final Logger logger = LoggerFactory.getLogger(RatingService.class);

    private final RatingRepository ratingRepository;
    private final UserService userService;
    private final ItemService itemService;
    private final BookingRepository bookingRepository;

    @Autowired
    public RatingService(RatingRepository ratingRepository, UserService userService, ItemService itemService,
            BookingRepository bookingRepository) {
        this.ratingRepository = ratingRepository;
        this.userService = userService;
        this.itemService = itemService;
        this.bookingRepository = bookingRepository;
    }

    public Rating createRating(Long senderId, RatingType ratingType, Long ratedId, Integer rate, String comment) {
        validateSender(senderId);
        validateRatedEntity(ratingType, ratedId, senderId);
        validateRatingDetails(rate, comment);

        Rating rating = new Rating(senderId, ratingType, ratedId, rate, comment);
        Rating savedRating = ratingRepository.save(rating);

        if (ratingType == RatingType.PRODUCT) {
            updateItemAverageRating(ratedId);
        } else if (ratingType == RatingType.OWNER) {
            updateOwnerAverageRating(ratedId);
        }

        logger.info("Rating created successfully with ID {}", savedRating.getId());
        return savedRating;
    }

    private void updateItemAverageRating(Long itemId) {
        List<Rating> ratings = ratingRepository.findByRatingTypeAndRatedId(RatingType.PRODUCT, itemId);
        if (ratings.isEmpty()) {
            return;
        }

        double average = ratings.stream()
                .mapToInt(Rating::getRate)
                .average()
                .orElse(0.0);

        // Round to 1 decimal place
        average = Math.round(average * 10.0) / 10.0;

        Item item = itemService.getItemById(itemId);
        if (item != null) {
            item.setAverageRating(average);
            itemService.saveItem(item);
            logger.info("Updated average rating for item {} to {}", itemId, average);
        }
    }

    private void updateOwnerAverageRating(Long ownerId) {
        List<Rating> ratings = ratingRepository.findByRatingTypeAndRatedId(RatingType.OWNER, ownerId);
        if (ratings.isEmpty()) {
            return;
        }

        double average = ratings.stream()
                .mapToInt(Rating::getRate)
                .average()
                .orElse(0.0);

        // Round to 1 decimal place
        average = Math.round(average * 10.0) / 10.0;

        Optional<User> userOpt = userService.getUserById(ownerId);
        if (userOpt.isPresent()) {
            User owner = userOpt.get();
            owner.setAverageRating(average);
            userService.saveUser(owner);
            logger.info("Updated average rating for owner {} to {}", ownerId, average);
        }
    }

    private void validateSender(Long senderId) {
        if (senderId == null || senderId < 0) {
            logger.warn("Failed to create rating: SenderId {} is invalid.", senderId);
            throw new IllegalArgumentException("Failed to create rating: SenderId " + senderId + " is invalid.");
        }

        Optional<User> optSender = userService.getUserById(senderId);
        if (optSender.isEmpty() || !UserRoles.RENTER.equals(optSender.get().getRole())) {
            logger.warn("Failed to create rating: Sender with ID {} is not a renter.", senderId);
            throw new IllegalArgumentException(
                    "Failed to create rating: Sender with ID " + senderId + " is not a renter.");
        }
    }

    private void validateRatedEntity(RatingType ratingType, Long ratedId, Long senderId) {
        if (ratedId == null || ratedId < 0) {
            logger.warn("Failed to create rating: RatedId {} is invalid.", ratedId);
            throw new IllegalArgumentException("Failed to create rating: RatedId " + ratedId + " is invalid.");
        }

        if (ratingType == RatingType.OWNER) {
            Optional<User> optUser = userService.getUserById(ratedId);
            if (optUser.isEmpty() || !optUser.get().getRole().equals(UserRoles.OWNER)) {
                logger.warn("Failed to create rating: Rated owner with ID {} does not exist.", ratedId);
                throw new IllegalArgumentException(
                        "Failed to create rating: Rated owner with ID " + ratedId + " does not exist.");
            }
            // Check if renter has completed booking with this owner
            if (!hasCompletedBookingWithOwner(senderId, ratedId)) {
                logger.warn("Failed to create rating: User {} has no completed booking with owner {}.", senderId,
                        ratedId);
                throw new IllegalArgumentException(
                        "You can only rate owners you have completed bookings with.");
            }
        } else if (ratingType == RatingType.PRODUCT) {
            Item item = itemService.getItemById(ratedId);
            if (item == null) {
                logger.warn("Failed to create rating: Rated item with ID {} does not exist.", ratedId);
                throw new IllegalArgumentException(
                        "Failed to create rating: Rated item with ID " + ratedId + " does not exist.");
            }
            // Prevent owners from rating their own products
            if (item.getOwnerId() != null && item.getOwnerId().equals(senderId)) {
                logger.warn("Failed to create rating: User {} cannot rate their own product {}.", senderId, ratedId);
                throw new IllegalArgumentException(
                        "Failed to create rating: Cannot rate your own product.");
            }
            // Check if renter has completed booking for this item
            if (!hasCompletedBooking(senderId, ratedId)) {
                logger.warn("Failed to create rating: User {} has no completed booking for item {}.", senderId,
                        ratedId);
                throw new IllegalArgumentException(
                        "You can only rate items you have rented and whose rental period has ended.");
            }
        } else {
            logger.warn("Failed to create rating: Unknown RatingType {}.", ratingType);
            throw new IllegalArgumentException("Failed to create rating: Unknown RatingType " + ratingType + ".");
        }
    }

    private void validateRatingDetails(Integer rate, String comment) {
        if (rate < 1 || rate > 5) {
            logger.warn("Failed to create rating: Rate {} is out of bounds (1-5).", rate);
            throw new IllegalArgumentException("Failed to create rating: Rate " + rate + " is out of bounds (1-5).");
        }
        if (comment != null && comment.length() > 512) {
            logger.warn("Failed to create rating: Comment length {} is invalid (0-512).", comment.length());
            throw new IllegalArgumentException(
                    "Failed to create rating: Comment length " + comment.length() + " is invalid (0-512).");
        }
    }

    private boolean hasCompletedBooking(Long renterId, Long itemId) {
        return bookingRepository.existsByRenterIdAndItem_IdAndStatusAndEndDateBefore(
                renterId, itemId, BookingStatus.ACCEPTED, LocalDate.now());
    }

    private boolean hasCompletedBookingWithOwner(Long renterId, Long ownerId) {
        return bookingRepository.existsByRenterIdAndItem_OwnerIdAndStatusAndEndDateBefore(
                renterId, ownerId, BookingStatus.ACCEPTED, LocalDate.now());
    }

    public List<Rating> getAllRatings() {
        return ratingRepository.findAll();
    }

    public Optional<Rating> getRatingById(Long id) {
        return ratingRepository.findById(id);
    }

    public List<Rating> getRatingBySenderId(Long senderId) {
        return ratingRepository.findBySenderId(senderId);
    }

    public List<Rating> getRatingByRatedInfo(RatingType ratingType, Long ratedId) {
        return ratingRepository.findByRatingTypeAndRatedId(ratingType, ratedId);
    }

    public Optional<Rating> getRatingBySenderIdAndRatedInfo(Long senderId, RatingType ratingType, Long ratedId) {
        return ratingRepository.findBySenderIdAndRatingTypeAndRatedId(senderId, ratingType, ratedId);
    }

    public boolean deleteRating(Long id) {
        if (ratingRepository.existsById(id)) {
            ratingRepository.deleteById(id);
            logger.info("Deleted rating {}", id);
            return true;
        }
        logger.warn("Failed to delete rating: Rating with ID {} not found.", id);
        return false;
    }

}
