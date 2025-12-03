package tqs.backend.tqsbackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ratings")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "rating_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RatingType ratingType;

    @Column(name = "rated_id", nullable = false)
    private Long ratedId;

    @Column(name = "rate", nullable = false)
    private Integer rate;

    @Column(name = "comment", nullable = true, length = 512)
    private String comment;

    public Rating() { }

    public Rating(Long senderId, RatingType ratingType, Long ratedId, Integer rate, String comment) {
        this.senderId = senderId;
        this.ratingType = ratingType;
        this.ratedId = ratedId;
        this.rate = rate;
        this.comment = comment;
    }

    public Long getId() { return id; }

    public Long getSenderId() { return senderId; }

    public RatingType getRatingType() { return ratingType; }

    public Long getRatedId() { return ratedId; }

    public Integer getRate() { return rate; }

    public String getComment() { return comment; }

    public void setId(Long id) { this.id = id; }

    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public void setRatingType(RatingType ratingType) { this.ratingType = ratingType; }

    public void setRatedId(Long ratedId) { this.ratedId = ratedId; }

    public void setRate(Integer rate) { this.rate = rate; }

    public void setComment(String comment) { this.comment = comment; }
 
}
