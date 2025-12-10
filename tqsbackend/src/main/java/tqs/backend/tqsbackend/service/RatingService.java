package tqs.backend.tqsbackend.service;

import java.util.Optional;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.entity.Rating;
import tqs.backend.tqsbackend.entity.RatingType;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.repository.RatingRepository;

@Service
public class RatingService {
    private static final Logger logger = LoggerFactory.getLogger(RatingService.class);

    private final RatingRepository ratingRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Autowired
    public RatingService(RatingRepository ratingRepository, UserService userService, ItemService itemService) {
        this.ratingRepository = ratingRepository;
        this.userService = userService;
        this.itemService = itemService;
    }

    public Rating createRating(Long senderId, RatingType ratingType, Long ratedId, Integer rate, String comment) {
        validateSender(senderId);
        validateRatedEntity(ratingType, ratedId, senderId);
        validateRatingDetails(rate, comment);

        Rating rating = new Rating(senderId, ratingType, ratedId, rate, comment);
        Rating savedRating = ratingRepository.save(rating);

        if (ratingType == RatingType.PRODUCT) {
            updateItemAverageRating(ratedId);
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
