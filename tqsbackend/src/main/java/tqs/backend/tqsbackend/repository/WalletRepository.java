package tqs.backend.tqsbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.Wallet;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByOwnerId(Long ownerId);
    Optional<Wallet> findByOwner(User owner);
    boolean existsByOwnerId(Long ownerId);
}
