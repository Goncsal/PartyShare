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
import tqs.backend.tqsbackend.fixtures.RatingTestFixtures;
import tqs.backend.tqsbackend.repository.RatingRepository;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private UserService userService;

    @Mock
    private ItemService itemService;

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

        Rating rating = new Rating(senderId, RatingType.PRODUCT, itemId, 5, "Great item!");
        when(ratingRepository.save(any(Rating.class))).thenReturn(rating);

        Rating r1 = new Rating(senderId, RatingType.PRODUCT, itemId, 5, "Great!");
        Rating r2 = new Rating(senderId, RatingType.PRODUCT, itemId, 3, "Okay.");
        when(ratingRepository.findByRatingTypeAndRatedId(RatingType.PRODUCT, itemId)).thenReturn(Arrays.asList(r1, r2));

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
                .hasMessageContaining("Sender with ID " + senderId + " is not a renter");
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
    }
}
