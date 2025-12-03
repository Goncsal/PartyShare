package tqs.backend.tqsbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tqs.backend.tqsbackend.entity.Rating;
import tqs.backend.tqsbackend.entity.RatingType;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findBySenderId(Long senderId);
    List<Rating> findByRatingTypeAndRatedId(RatingType ratingType, Long ratedId);
    Optional<Rating> findBySenderIdAndRatingTypeAndRatedId(Long senderId, RatingType ratingType, Long ratedId);
}
