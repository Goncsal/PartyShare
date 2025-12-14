package tqs.backend.tqsbackend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private Long renterId;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal dailyPrice;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    private String paymentReference;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Dual confirmation for wallet release
    @Column(nullable = false)
    private boolean renterConfirmed = false;

    @Column(nullable = false)
    private boolean ownerConfirmed = false;

    @Column
    private LocalDateTime returnedAt;

    public Booking(Item item, Long renterId, LocalDate startDate, LocalDate endDate, BigDecimal dailyPrice,
            BigDecimal totalPrice, BookingStatus status, PaymentStatus paymentStatus) {
        this.item = item;
        this.renterId = renterId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dailyPrice = dailyPrice;
        this.totalPrice = totalPrice;
        this.status = status;
        this.paymentStatus = paymentStatus;
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
