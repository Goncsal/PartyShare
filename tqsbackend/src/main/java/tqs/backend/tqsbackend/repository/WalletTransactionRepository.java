package tqs.backend.tqsbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tqs.backend.tqsbackend.entity.Booking;
import tqs.backend.tqsbackend.entity.TransactionStatus;
import tqs.backend.tqsbackend.entity.Wallet;
import tqs.backend.tqsbackend.entity.WalletTransaction;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    Optional<WalletTransaction> findByBookingId(Long bookingId);
    Optional<WalletTransaction> findByBooking(Booking booking);
    List<WalletTransaction> findByWallet(Wallet wallet);
    List<WalletTransaction> findByWalletId(Long walletId);
    List<WalletTransaction> findByWalletIdAndStatus(Long walletId, TransactionStatus status);
}
