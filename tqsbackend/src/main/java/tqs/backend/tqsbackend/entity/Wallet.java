package tqs.backend.tqsbackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets")
@Getter
@Setter
@NoArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "owner_id", nullable = false, unique = true)
    private User owner;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal pendingBalance = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Wallet(User owner) {
        this.owner = owner;
        this.balance = BigDecimal.ZERO;
        this.pendingBalance = BigDecimal.ZERO;
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

    public void addPendingFunds(BigDecimal amount) {
        this.pendingBalance = this.pendingBalance.add(amount);
    }

    public void releasePendingFunds(BigDecimal amount) {
        this.pendingBalance = this.pendingBalance.subtract(amount);
        this.balance = this.balance.add(amount);
    }

    public void refundPendingFunds(BigDecimal amount) {
        this.pendingBalance = this.pendingBalance.subtract(amount);
    }
}
