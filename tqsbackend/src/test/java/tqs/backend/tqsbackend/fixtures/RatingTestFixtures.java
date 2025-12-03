package tqs.backend.tqsbackend.fixtures;

import tqs.backend.tqsbackend.entity.Rating;
import tqs.backend.tqsbackend.entity.RatingType;

public final class RatingTestFixtures {

    private RatingTestFixtures() {
    }

    public static Rating sampleRating(long id, long senderId, long ratedId) {
        Rating rating = new Rating(senderId, RatingType.OWNER, ratedId, 5, "Great service!");
        rating.setId(id);
        return rating;
    }

    public static Rating sampleRating(long senderId, long ratedId) {
        return new Rating(senderId, RatingType.OWNER, ratedId, 5, "Great service!");
    }
}
