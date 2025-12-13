package tqs.backend.tqsbackend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.List;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tqs.backend.tqsbackend.entity.Rating;
import tqs.backend.tqsbackend.entity.RatingType;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.entity.BookingStatus;
import tqs.backend.tqsbackend.fixtures.RatingTestFixtures;
import tqs.backend.tqsbackend.repository.RatingRepository;
import tqs.backend.tqsbackend.repository.BookingRepository;
import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

        @Mock
        private RatingRepository ratingRepository;

        @Mock
        private UserService userService;

        @Mock
        private ItemService itemService;

        @Mock
        private BookingRepository bookingRepository;

        @InjectMocks
        private RatingService ratingService;

        @Test
        void createRating_Success() {
                Long senderId = 1L;
                Long ratedId = 2L;
                User sender = new User();
                sender.setId(senderId);
                sender.setRole(UserRoles.RENTER);

                User rated = new User();
                rated.setId(ratedId);
                rated.setRole(UserRoles.OWNER);

                when(userService.getUserById(senderId)).thenReturn(Optional.of(sender));
                when(userService.getUserById(ratedId)).thenReturn(Optional.of(rated));
                when(bookingRepository.existsByRenterIdAndItem_OwnerIdAndStatusAndEndDateBefore(
                                any(Long.class), any(Long.class), any(BookingStatus.class), any(LocalDate.class)))
                                .thenReturn(true);

                Rating rating = RatingTestFixtures.sampleRating(senderId, ratedId);
                when(ratingRepository.save(any(Rating.class))).thenReturn(rating);

                Rating created = ratingService.createRating(senderId, RatingType.OWNER, ratedId, 5, "Great service!");

                assertThat(created).isNotNull();
                assertThat(created.getRate()).isEqualTo(5);
                verify(ratingRepository).save(any(Rating.class));
        }

        @Test
        void createRating_Product_UpdatesAverage() {
                Long senderId = 1L;
                Long itemId = 10L;
                User sender = new User();
                sender.setId(senderId);
                sender.setRole(UserRoles.RENTER);

                tqs.backend.tqsbackend.entity.Item item = new tqs.backend.tqsbackend.entity.Item();
                item.setId(itemId);

                when(userService.getUserById(senderId)).thenReturn(Optional.of(sender));
                when(itemService.getItemById(itemId)).thenReturn(item);
                when(bookingRepository.existsByRenterIdAndItem_IdAndStatusAndEndDateBefore(
                                any(Long.class), any(Long.class), any(BookingStatus.class), any(LocalDate.class)))
                                .thenReturn(true);

                Rating rating = new Rating(senderId, RatingType.PRODUCT, itemId, 5, "Great item!");
                when(ratingRepository.save(any(Rating.class))).thenReturn(rating);

                Rating r1 = new Rating(senderId, RatingType.PRODUCT, itemId, 5, "Great!");
                Rating r2 = new Rating(senderId, RatingType.PRODUCT, itemId, 3, "Okay.");
                when(ratingRepository.findByRatingTypeAndRatedId(RatingType.PRODUCT, itemId))
                                .thenReturn(Arrays.asList(r1, r2));

                ratingService.createRating(senderId, RatingType.PRODUCT, itemId, 5, "Great item!");

                verify(itemService).saveItem(any(tqs.backend.tqsbackend.entity.Item.class));
                assertThat(item.getAverageRating()).isEqualTo(4.0);
        }

        @Test
        void createRating_InvalidSender_ThrowsException() {
                Long senderId = 1L;
                when(userService.getUserById(senderId)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> ratingService.createRating(senderId, RatingType.OWNER, 2L, 5, "Comment"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Sender with ID " + senderId + " does not exist");
        }

        @Test
        void createRating_SenderNotRenter_ThrowsException() {
                Long senderId = 1L;
                User sender = new User();
                sender.setId(senderId);
                sender.setRole(UserRoles.OWNER); // Wrong role

                when(userService.getUserById(senderId)).thenReturn(Optional.of(sender));

                assertThatThrownBy(() -> ratingService.createRating(senderId, RatingType.OWNER, 2L, 5, "Comment"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Sender with ID " + senderId + " is not a renter");
        }

        @Test
        void createRating_InvalidRatedOwner_ThrowsException() {
                Long senderId = 1L;
                Long ratedId = 2L;
                User sender = new User();
                sender.setId(senderId);
                sender.setRole(UserRoles.RENTER);

                when(userService.getUserById(senderId)).thenReturn(Optional.of(sender));
                when(userService.getUserById(ratedId)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> ratingService.createRating(senderId, RatingType.OWNER, ratedId, 5, "Comment"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Rated owner with ID " + ratedId + " does not exist");
        }

        @Test
        void createRating_RatedUserNotOwner_ThrowsException() {
                Long senderId = 1L;
                Long ratedId = 2L;
                User sender = new User();
                sender.setId(senderId);
                sender.setRole(UserRoles.RENTER);

                User rated = new User();
                rated.setId(ratedId);
                rated.setRole(UserRoles.RENTER); // Wrong role

                when(userService.getUserById(senderId)).thenReturn(Optional.of(sender));
                when(userService.getUserById(ratedId)).thenReturn(Optional.of(rated));

                assertThatThrownBy(() -> ratingService.createRating(senderId, RatingType.OWNER, ratedId, 5, "Comment"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Rated owner with ID " + ratedId + " does not exist");
        }

        @Test
        void createRating_InvalidRatedItem_ThrowsException() {
                Long senderId = 1L;
                Long itemId = 10L;
                User sender = new User();
                sender.setId(senderId);
                sender.setRole(UserRoles.RENTER);

                when(userService.getUserById(senderId)).thenReturn(Optional.of(sender));
                when(itemService.getItemById(itemId)).thenReturn(null);

                assertThatThrownBy(() -> ratingService.createRating(senderId, RatingType.PRODUCT, itemId, 5, "Comment"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Rated item with ID " + itemId + " does not exist");
        }

        @Test
        void createRating_InvalidRate_ThrowsException() {
                Long senderId = 1L;
                Long ratedId = 2L;
                User sender = new User();
                sender.setId(senderId);
                sender.setRole(UserRoles.RENTER);

                User rated = new User();
                rated.setId(ratedId);
                rated.setRole(UserRoles.OWNER);

                when(userService.getUserById(senderId)).thenReturn(Optional.of(sender));
                when(userService.getUserById(ratedId)).thenReturn(Optional.of(rated));
                when(bookingRepository.existsByRenterIdAndItem_OwnerIdAndStatusAndEndDateBefore(
                                any(Long.class), any(Long.class), any(BookingStatus.class), any(LocalDate.class)))
                                .thenReturn(true);

                assertThatThrownBy(() -> ratingService.createRating(senderId, RatingType.OWNER, ratedId, 6, "Comment"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("out of bounds");
        }

        @Test
        void getRatingById_ReturnsRating() {
                Rating rating = RatingTestFixtures.sampleRating(1L, 1L, 2L);
                when(ratingRepository.findById(1L)).thenReturn(Optional.of(rating));

                Optional<Rating> found = ratingService.getRatingById(1L);
                assertThat(found).isPresent();
                assertThat(found.get().getId()).isEqualTo(1L);
        }

        @Test
        void deleteRating_Success() {
                when(ratingRepository.existsById(1L)).thenReturn(true);

                boolean result = ratingService.deleteRating(1L);

                assertThat(result).isTrue();
                verify(ratingRepository).deleteById(1L);
        }

        @Test
        void deleteRating_NotFound() {
                when(ratingRepository.existsById(1L)).thenReturn(false);

                boolean result = ratingService.deleteRating(1L);

                assertThat(result).isFalse();
        }

        @Test
        void getAllRatings_ReturnsList() {
                Rating r1 = RatingTestFixtures.sampleRating(1L, 1L, 2L);
                Rating r2 = RatingTestFixtures.sampleRating(2L, 1L, 3L);
                when(ratingRepository.findAll()).thenReturn(Arrays.asList(r1, r2));

                List<Rating> ratings = ratingService.getAllRatings();
                assertThat(ratings).hasSize(2);
        }

        @Test
        void getRatingBySenderId_ReturnsList() {
                Rating r1 = RatingTestFixtures.sampleRating(1L, 1L, 2L);
                when(ratingRepository.findBySenderId(1L)).thenReturn(Arrays.asList(r1));

                List<Rating> ratings = ratingService.getRatingBySenderId(1L);
                assertThat(ratings).hasSize(1);
                assertThat(ratings.get(0)).isEqualTo(r1);
                verify(ratingRepository).findBySenderId(1L);
        }

        @Test
        void createRating_OwnerRatingOwnProduct_ThrowsException() {
                // Arrange
                Long ownerId = 1L;
                Long itemId = 10L;
                User owner = new User();
                owner.setId(ownerId);
                owner.setRole(UserRoles.RENTER);

                tqs.backend.tqsbackend.entity.Item item = new tqs.backend.tqsbackend.entity.Item();
                item.setId(itemId);
                item.setOwnerId(ownerId);

                when(userService.getUserById(ownerId)).thenReturn(Optional.of(owner));
                when(itemService.getItemById(itemId)).thenReturn(item);

                // Act & Assert
                assertThatThrownBy(() -> ratingService.createRating(ownerId, RatingType.PRODUCT, itemId, 5, "Great!"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Cannot rate your own product");

                verify(ratingRepository, org.mockito.Mockito.never()).save(any(Rating.class));
        }

        @Test
        void createRating_Owner_UpdatesAverageRating() {
                // Arrange
                Long senderId = 1L;
                Long ownerId = 2L;
                User sender = new User();
                sender.setId(senderId);
                sender.setRole(UserRoles.RENTER);

                User owner = new User();
                owner.setId(ownerId);
                owner.setRole(UserRoles.OWNER);

                when(userService.getUserById(senderId)).thenReturn(Optional.of(sender));
                when(userService.getUserById(ownerId)).thenReturn(Optional.of(owner));
                when(bookingRepository.existsByRenterIdAndItem_OwnerIdAndStatusAndEndDateBefore(
                                any(Long.class), any(Long.class), any(BookingStatus.class), any(LocalDate.class)))
                                .thenReturn(true);

                Rating rating = new Rating(senderId, RatingType.OWNER, ownerId, 5, "Great owner!");
                when(ratingRepository.save(any(Rating.class))).thenReturn(rating);

                // Create existing ratings for the owner
                Rating r1 = new Rating(senderId, RatingType.OWNER, ownerId, 5, "Great!");
                Rating r2 = new Rating(senderId, RatingType.OWNER, ownerId, 3, "Okay.");
                when(ratingRepository.findByRatingTypeAndRatedId(RatingType.OWNER, ownerId))
                                .thenReturn(Arrays.asList(r1, r2));

                // Act
                ratingService.createRating(senderId, RatingType.OWNER, ownerId, 5, "Great owner!");

                // Assert
                verify(userService).saveUser(any(User.class));
                assertThat(owner.getAverageRating()).isEqualTo(4.0);
        }

        @Test
        void updateOwnerAverageRating_WithMultipleRatings_CalculatesCorrectAverage() {
                // Arrange
                Long senderId = 1L;
                Long ownerId = 2L;
                User sender = new User();
                sender.setId(senderId);
                sender.setRole(UserRoles.RENTER);

                User owner = new User();
                owner.setId(ownerId);
                owner.setRole(UserRoles.OWNER);

                when(userService.getUserById(senderId)).thenReturn(Optional.of(sender));
                when(userService.getUserById(ownerId)).thenReturn(Optional.of(owner));
                when(bookingRepository.existsByRenterIdAndItem_OwnerIdAndStatusAndEndDateBefore(
                                any(Long.class), any(Long.class), any(BookingStatus.class), any(LocalDate.class)))
                                .thenReturn(true);

                Rating rating = new Rating(senderId, RatingType.OWNER, ownerId, 4, "Good!");
                when(ratingRepository.save(any(Rating.class))).thenReturn(rating);

                // Create multiple ratings with different values
                Rating r1 = new Rating(senderId, RatingType.OWNER, ownerId, 5, "Excellent!");
                Rating r2 = new Rating(senderId, RatingType.OWNER, ownerId, 3, "Average");
                Rating r3 = new Rating(senderId, RatingType.OWNER, ownerId, 4, "Good");
                when(ratingRepository.findByRatingTypeAndRatedId(RatingType.OWNER, ownerId))
                                .thenReturn(Arrays.asList(r1, r2, r3));

                // Act
                ratingService.createRating(senderId, RatingType.OWNER, ownerId, 4, "Good!");

                // Assert - Average should be (5 + 3 + 4) / 3 = 4.0
                verify(userService).saveUser(any(User.class));
                assertThat(owner.getAverageRating()).isEqualTo(4.0);
        }

        @Test
        void updateOwnerAverageRating_RoundsToOneDecimalPlace() {
                // Arrange
                Long senderId = 1L;
                Long ownerId = 2L;
                User sender = new User();
                sender.setId(senderId);
                sender.setRole(UserRoles.RENTER);

                User owner = new User();
                owner.setId(ownerId);
                owner.setRole(UserRoles.OWNER);

                when(userService.getUserById(senderId)).thenReturn(Optional.of(sender));
                when(userService.getUserById(ownerId)).thenReturn(Optional.of(owner));
                when(bookingRepository.existsByRenterIdAndItem_OwnerIdAndStatusAndEndDateBefore(
                                any(Long.class), any(Long.class), any(BookingStatus.class), any(LocalDate.class)))
                                .thenReturn(true);

                Rating rating = new Rating(senderId, RatingType.OWNER, ownerId, 4, "Good!");
                when(ratingRepository.save(any(Rating.class))).thenReturn(rating);

                // Create ratings that result in a value needing rounding
                Rating r1 = new Rating(senderId, RatingType.OWNER, ownerId, 5, "Excellent!");
                Rating r2 = new Rating(senderId, RatingType.OWNER, ownerId, 4, "Good");
                Rating r3 = new Rating(senderId, RatingType.OWNER, ownerId, 4, "Good");
                when(ratingRepository.findByRatingTypeAndRatedId(RatingType.OWNER, ownerId))
                                .thenReturn(Arrays.asList(r1, r2, r3));

                // Act
                ratingService.createRating(senderId, RatingType.OWNER, ownerId, 4, "Good!");

                // Assert - Average should be (5 + 4 + 4) / 3 = 4.333... rounded to 4.3
                verify(userService).saveUser(any(User.class));
                assertThat(owner.getAverageRating()).isEqualTo(4.3);
        }

        @Test
        void updateOwnerAverageRating_WithSingleRating() {
                // Arrange
                Long senderId = 1L;
                Long ownerId = 2L;
                User sender = new User();
                sender.setId(senderId);
                sender.setRole(UserRoles.RENTER);

                User owner = new User();
                owner.setId(ownerId);
                owner.setRole(UserRoles.OWNER);

                when(userService.getUserById(senderId)).thenReturn(Optional.of(sender));
                when(userService.getUserById(ownerId)).thenReturn(Optional.of(owner));
                when(bookingRepository.existsByRenterIdAndItem_OwnerIdAndStatusAndEndDateBefore(
                                any(Long.class), any(Long.class), any(BookingStatus.class), any(LocalDate.class)))
                                .thenReturn(true);

                Rating rating = new Rating(senderId, RatingType.OWNER, ownerId, 5, "Perfect!");
                when(ratingRepository.save(any(Rating.class))).thenReturn(rating);

                // Only one rating
                Rating r1 = new Rating(senderId, RatingType.OWNER, ownerId, 5, "Perfect!");
                when(ratingRepository.findByRatingTypeAndRatedId(RatingType.OWNER, ownerId))
                                .thenReturn(Arrays.asList(r1));

                // Act
                ratingService.createRating(senderId, RatingType.OWNER, ownerId, 5, "Perfect!");

                // Assert - Average should be 5.0
                verify(userService).saveUser(any(User.class));
                assertThat(owner.getAverageRating()).isEqualTo(5.0);
        }

        @Test
        void updateOwnerAverageRating_WithEmptyRatings_DoesNotUpdateOwner() {
                // Arrange
                Long senderId = 1L;
                Long ownerId = 2L;
                User sender = new User();
                sender.setId(senderId);
                sender.setRole(UserRoles.RENTER);

                User owner = new User();
                owner.setId(ownerId);
                owner.setRole(UserRoles.OWNER);

                when(userService.getUserById(senderId)).thenReturn(Optional.of(sender));
                when(userService.getUserById(ownerId)).thenReturn(Optional.of(owner));
                when(bookingRepository.existsByRenterIdAndItem_OwnerIdAndStatusAndEndDateBefore(
                                any(Long.class), any(Long.class), any(BookingStatus.class), any(LocalDate.class)))
                                .thenReturn(true);

                Rating rating = new Rating(senderId, RatingType.OWNER, ownerId, 5, "Great!");
                when(ratingRepository.save(any(Rating.class))).thenReturn(rating);

                // Empty ratings list
                when(ratingRepository.findByRatingTypeAndRatedId(RatingType.OWNER, ownerId))
                                .thenReturn(Arrays.asList());

                // Act
                ratingService.createRating(senderId, RatingType.OWNER, ownerId, 5, "Great!");

                // Assert - saveUser should not be called when ratings list is empty
                verify(userService, org.mockito.Mockito.never()).saveUser(any(User.class));
        }

        @Test
        void updateOwnerAverageRating_WithNonExistentOwner_DoesNotUpdateOwner() {
                // Arrange
                Long senderId = 1L;
                Long ownerId = 2L;
                User sender = new User();
                sender.setId(senderId);
                sender.setRole(UserRoles.RENTER);

                when(userService.getUserById(senderId)).thenReturn(Optional.of(sender));
                // First call returns owner for validation, second call returns empty for update
                when(userService.getUserById(ownerId))
                                .thenReturn(Optional.of(new User() {
                                        {
                                                setId(ownerId);
                                                setRole(UserRoles.OWNER);
                                        }
                                }))
                                .thenReturn(Optional.empty());
                when(bookingRepository.existsByRenterIdAndItem_OwnerIdAndStatusAndEndDateBefore(
                                any(Long.class), any(Long.class), any(BookingStatus.class), any(LocalDate.class)))
                                .thenReturn(true);

                Rating rating = new Rating(senderId, RatingType.OWNER, ownerId, 5, "Great!");
                when(ratingRepository.save(any(Rating.class))).thenReturn(rating);

                // Ratings exist
                Rating r1 = new Rating(senderId, RatingType.OWNER, ownerId, 5, "Great!");
                when(ratingRepository.findByRatingTypeAndRatedId(RatingType.OWNER, ownerId))
                                .thenReturn(Arrays.asList(r1));

                // Act
                ratingService.createRating(senderId, RatingType.OWNER, ownerId, 5, "Great!");

                // Assert - saveUser should not be called when owner is not found
                verify(userService, org.mockito.Mockito.never()).saveUser(any(User.class));
        }

        // ========== EXTRA TESTS FOR validateRatedEntity ==========

        @Test
        void createRating_Renter_InvalidRatedId_Null_ThrowsException() {
                Long senderId = 1L;
                User sender = new User();
                sender.setId(senderId);
                sender.setRole(UserRoles.OWNER);

                when(userService.getUserById(senderId)).thenReturn(Optional.of(sender));

                assertThatThrownBy(() -> ratingService.createRating(senderId, RatingType.RENTER, null, 5, "Comment"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("RatedId null is invalid");
        }

        @Test
        void createRating_Renter_InvalidRatedId_Negative_ThrowsException() {
                Long senderId = 1L;
                User sender = new User();
                sender.setId(senderId);
                sender.setRole(UserRoles.OWNER);

                when(userService.getUserById(senderId)).thenReturn(Optional.of(sender));

                assertThatThrownBy(() -> ratingService.createRating(senderId, RatingType.RENTER, -1L, 5, "Comment"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("RatedId -1 is invalid");
        }

        @Test
        void createRating_Renter_NonExistentRenter_ThrowsException() {
                Long senderId = 1L;
                Long renterId = 2L;
                User sender = new User();
                sender.setId(senderId);
                sender.setRole(UserRoles.OWNER);

                when(userService.getUserById(senderId)).thenReturn(Optional.of(sender));
                when(userService.getUserById(renterId)).thenReturn(Optional.empty());

                assertThatThrownBy(
                                () -> ratingService.createRating(senderId, RatingType.RENTER, renterId, 5, "Comment"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Rated renter with ID " + renterId + " does not exist");
        }

        @Test
        void createRating_Renter_WrongRenterRole_ThrowsException() {
                Long senderId = 1L;
                Long renterId = 2L;
                User sender = new User();
                sender.setId(senderId);
                sender.setRole(UserRoles.OWNER);

                User renter = new User();
                renter.setId(renterId);
                renter.setRole(UserRoles.OWNER); // Wrong role - should be RENTER

                when(userService.getUserById(senderId)).thenReturn(Optional.of(sender));
                when(userService.getUserById(renterId)).thenReturn(Optional.of(renter));

                assertThatThrownBy(
                                () -> ratingService.createRating(senderId, RatingType.RENTER, renterId, 5, "Comment"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Rated renter with ID " + renterId + " does not exist");
        }

        @Test
        void createRating_Renter_NoCompletedBooking_ThrowsException() {
                Long senderId = 1L;
                Long renterId = 2L;
                User sender = new User();
                sender.setId(senderId);
                sender.setRole(UserRoles.OWNER);

                User renter = new User();
                renter.setId(renterId);
                renter.setRole(UserRoles.RENTER);

                when(userService.getUserById(senderId)).thenReturn(Optional.of(sender));
                when(userService.getUserById(renterId)).thenReturn(Optional.of(renter));
                when(bookingRepository.existsByRenterIdAndItem_OwnerIdAndStatusAndEndDateBefore(
                                renterId, senderId, BookingStatus.ACCEPTED, LocalDate.now())).thenReturn(false);

                assertThatThrownBy(() -> ratingService.createRating(senderId, RatingType.RENTER, renterId, 5,
                                "Great renter!"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("You can only rate renters you have completed bookings with");
        }

        @Test
        void createRating_Product_NoCompletedBooking_ThrowsException() {
                Long senderId = 1L;
                Long itemId = 10L;
                User sender = new User();
                sender.setId(senderId);
                sender.setRole(UserRoles.RENTER);

                tqs.backend.tqsbackend.entity.Item item = new tqs.backend.tqsbackend.entity.Item();
                item.setId(itemId);
                item.setOwnerId(99L); // Different owner

                when(userService.getUserById(senderId)).thenReturn(Optional.of(sender));
                when(itemService.getItemById(itemId)).thenReturn(item);
                when(bookingRepository.existsByRenterIdAndItem_IdAndStatusAndEndDateBefore(
                                senderId, itemId, BookingStatus.ACCEPTED, LocalDate.now())).thenReturn(false);

                assertThatThrownBy(() -> ratingService.createRating(senderId, RatingType.PRODUCT, itemId, 5, "Great!"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining(
                                                "You can only rate items you have rented and whose rental period has ended");
        }

        @Test
        void createRating_Owner_NoCompletedBooking_ThrowsException() {
                Long senderId = 1L;
                Long ownerId = 2L;
                User sender = new User();
                sender.setId(senderId);
                sender.setRole(UserRoles.RENTER);

                User owner = new User();
                owner.setId(ownerId);
                owner.setRole(UserRoles.OWNER);

                when(userService.getUserById(senderId)).thenReturn(Optional.of(sender));
                when(userService.getUserById(ownerId)).thenReturn(Optional.of(owner));
                when(bookingRepository.existsByRenterIdAndItem_OwnerIdAndStatusAndEndDateBefore(
                                senderId, ownerId, BookingStatus.ACCEPTED, LocalDate.now())).thenReturn(false);

                assertThatThrownBy(() -> ratingService.createRating(senderId, RatingType.OWNER, ownerId, 5, "Good!"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("You can only rate owners you have completed bookings with");
        }

        // ========== TESTS FOR updateRenterAverageRating ==========

        @Test
        void updateRenterAverageRating_WithSingleRating() {
                // Arrange
                Long senderId = 1L;
                Long renterId = 2L;
                User sender = new User();
                sender.setId(senderId);
                sender.setRole(UserRoles.OWNER);

                User renter = new User();
                renter.setId(renterId);
                renter.setRole(UserRoles.RENTER);

                when(userService.getUserById(senderId)).thenReturn(Optional.of(sender));
                when(userService.getUserById(renterId)).thenReturn(Optional.of(renter));
                when(bookingRepository.existsByRenterIdAndItem_OwnerIdAndStatusAndEndDateBefore(
                                renterId, senderId, BookingStatus.ACCEPTED, LocalDate.now())).thenReturn(true);

                Rating rating = new Rating(senderId, RatingType.RENTER, renterId, 5, "Perfect!");
                when(ratingRepository.save(any(Rating.class))).thenReturn(rating);

                // Only one rating
                Rating r1 = new Rating(senderId, RatingType.RENTER, renterId, 5, "Perfect!");
                when(ratingRepository.findByRatingTypeAndRatedId(RatingType.RENTER, renterId))
                                .thenReturn(Arrays.asList(r1));

                // Act
                ratingService.createRating(senderId, RatingType.RENTER, renterId, 5, "Perfect!");

                // Assert - Average should be 5.0
                verify(userService).saveUser(any(User.class));
                assertThat(renter.getAverageRating()).isEqualTo(5.0);
        }

        @Test
        void updateRenterAverageRating_WithMultipleRatings_CalculatesCorrectAverage() {
                // Arrange
                Long senderId = 1L;
                Long renterId = 2L;
                User sender = new User();
                sender.setId(senderId);
                sender.setRole(UserRoles.OWNER);

                User renter = new User();
                renter.setId(renterId);
                renter.setRole(UserRoles.RENTER);

                when(userService.getUserById(senderId)).thenReturn(Optional.of(sender));
                when(userService.getUserById(renterId)).thenReturn(Optional.of(renter));
                when(bookingRepository.existsByRenterIdAndItem_OwnerIdAndStatusAndEndDateBefore(
                                renterId, senderId, BookingStatus.ACCEPTED, LocalDate.now())).thenReturn(true);

                Rating rating = new Rating(senderId, RatingType.RENTER, renterId, 4, "Good!");
                when(ratingRepository.save(any(Rating.class))).thenReturn(rating);

                // Multiple ratings: 5, 3, 4
                Rating r1 = new Rating(senderId, RatingType.RENTER, renterId, 5, "Great");
                Rating r2 = new Rating(senderId, RatingType.RENTER, renterId, 3, "Average");
                Rating r3 = new Rating(senderId, RatingType.RENTER, renterId, 4, "Good");
                when(ratingRepository.findByRatingTypeAndRatedId(RatingType.RENTER, renterId))
                                .thenReturn(Arrays.asList(r1, r2, r3));

                // Act
                ratingService.createRating(senderId, RatingType.RENTER, renterId, 4, "Good!");

                // Assert - Average should be (5 + 3 + 4) / 3 = 4.0
                verify(userService).saveUser(any(User.class));
                assertThat(renter.getAverageRating()).isEqualTo(4.0);
        }

        @Test
        void updateRenterAverageRating_WithEmptyRatings_DoesNotUpdateRenter() {
                // Arrange
                Long senderId = 1L;
                Long renterId = 2L;
                User sender = new User();
                sender.setId(senderId);
                sender.setRole(UserRoles.OWNER);

                User renter = new User();
                renter.setId(renterId);
                renter.setRole(UserRoles.RENTER);

                when(userService.getUserById(senderId)).thenReturn(Optional.of(sender));
                when(userService.getUserById(renterId)).thenReturn(Optional.of(renter));
                when(bookingRepository.existsByRenterIdAndItem_OwnerIdAndStatusAndEndDateBefore(
                                renterId, senderId, BookingStatus.ACCEPTED, LocalDate.now())).thenReturn(true);

                Rating rating = new Rating(senderId, RatingType.RENTER, renterId, 5, "Great!");
                when(ratingRepository.save(any(Rating.class))).thenReturn(rating);

                // Empty ratings list
                when(ratingRepository.findByRatingTypeAndRatedId(RatingType.RENTER, renterId))
                                .thenReturn(Arrays.asList());

                // Act
                ratingService.createRating(senderId, RatingType.RENTER, renterId, 5, "Great!");

                // Assert - saveUser should not be called when ratings list is empty
                verify(userService, org.mockito.Mockito.never()).saveUser(any(User.class));
        }

        @Test
        void updateRenterAverageRating_WithNonExistentRenter_DoesNotUpdateRenter() {
                // Arrange
                Long senderId = 1L;
                Long renterId = 2L;
                User sender = new User();
                sender.setId(senderId);
                sender.setRole(UserRoles.OWNER);

                when(userService.getUserById(senderId)).thenReturn(Optional.of(sender));
                // First call returns renter for validation, second call returns empty for
                // update
                when(userService.getUserById(renterId))
                                .thenReturn(Optional.of(new User() {
                                        {
                                                setId(renterId);
                                                setRole(UserRoles.RENTER);
                                        }
                                }))
                                .thenReturn(Optional.empty());
                when(bookingRepository.existsByRenterIdAndItem_OwnerIdAndStatusAndEndDateBefore(
                                renterId, senderId, BookingStatus.ACCEPTED, LocalDate.now())).thenReturn(true);

                Rating rating = new Rating(senderId, RatingType.RENTER, renterId, 5, "Great!");
                when(ratingRepository.save(any(Rating.class))).thenReturn(rating);

                // Ratings exist
                Rating r1 = new Rating(senderId, RatingType.RENTER, renterId, 5, "Great!");
                when(ratingRepository.findByRatingTypeAndRatedId(RatingType.RENTER, renterId))
                                .thenReturn(Arrays.asList(r1));

                // Act
                ratingService.createRating(senderId, RatingType.RENTER, renterId, 5, "Great!");

                // Assert - saveUser should not be called when renter is not found
                verify(userService, org.mockito.Mockito.never()).saveUser(any(User.class));
        }

        @Test
        void updateRenterAverageRating_RoundsToOneDecimalPlace() {
                // Arrange
                Long senderId = 1L;
                Long renterId = 2L;
                User sender = new User();
                sender.setId(senderId);
                sender.setRole(UserRoles.OWNER);

                User renter = new User();
                renter.setId(renterId);
                renter.setRole(UserRoles.RENTER);

                when(userService.getUserById(senderId)).thenReturn(Optional.of(sender));
                when(userService.getUserById(renterId)).thenReturn(Optional.of(renter));
                when(bookingRepository.existsByRenterIdAndItem_OwnerIdAndStatusAndEndDateBefore(
                                renterId, senderId, BookingStatus.ACCEPTED, LocalDate.now())).thenReturn(true);

                Rating rating = new Rating(senderId, RatingType.RENTER, renterId, 4, "Good!");
                when(ratingRepository.save(any(Rating.class))).thenReturn(rating);

                // Multiple ratings: 5, 4, 4
                Rating r1 = new Rating(senderId, RatingType.RENTER, renterId, 5, "Great");
                Rating r2 = new Rating(senderId, RatingType.RENTER, renterId, 4, "Good");
                Rating r3 = new Rating(senderId, RatingType.RENTER, renterId, 4, "Good");
                when(ratingRepository.findByRatingTypeAndRatedId(RatingType.RENTER, renterId))
                                .thenReturn(Arrays.asList(r1, r2, r3));

                // Act
                ratingService.createRating(senderId, RatingType.RENTER, renterId, 4, "Good!");

                // Assert - Average should be (5 + 4 + 4) / 3 = 4.333... rounded to 4.3
                verify(userService).saveUser(any(User.class));
                assertThat(renter.getAverageRating()).isEqualTo(4.3);
        }
}
