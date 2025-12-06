package tqs.backend.tqsbackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tqs.backend.tqsbackend.entity.Rating;
import tqs.backend.tqsbackend.entity.RatingType;
import tqs.backend.tqsbackend.service.RatingService;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
public class RatingRestController {

    private final RatingService ratingService;

    public RatingRestController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping("/new")
    public ResponseEntity<Rating> createRating(@RequestBody Rating rating) {
        Rating created = ratingService.createRating(rating.getSenderId(), rating.getRatingType(), rating.getRatedId(),
                rating.getRate(), rating.getComment());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rating> getRatingById(@PathVariable Long id) {
        return ratingService.getRatingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRating(@PathVariable Long id) {
        if (ratingService.deleteRating(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public List<Rating> searchRatings(
            @RequestParam(required = false) Long sender,
            @RequestParam(required = false) RatingType type,
            @RequestParam(required = false) Long rated) {
        if (sender != null && type != null && rated != null) {
            return ratingService.getRatingBySenderIdAndRatedInfo(sender, type, rated).map(List::of).orElseGet(List::of);
        } else if (sender != null) {
            return ratingService.getRatingBySenderId(sender);
        } else if (type != null && rated != null) {
            return ratingService.getRatingByRatedInfo(type, rated);
        }
        return ratingService.getAllRatings();
    }
}
