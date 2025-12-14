package tqs.backend.tqsbackend.entity;

public enum TransactionStatus {
    PENDING,    // Aguarda confirmações de renter e owner
    RELEASED,   // Fundos libertados para owner
    REFUNDED,   // Fundos devolvidos ao renter
    DISPUTED    // Em disputa
}
