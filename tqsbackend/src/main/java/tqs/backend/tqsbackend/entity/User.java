package tqs.backend.tqsbackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRoles role;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "average_rating")
    private Double averageRating;

    public User() {
    }

    public User(String name, String email, String password, UserRoles role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.isActive = true;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public UserRoles getRole() {
        return role;
    }

    public boolean isActive() {
        return isActive;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(UserRoles role) {
        this.role = role;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
}