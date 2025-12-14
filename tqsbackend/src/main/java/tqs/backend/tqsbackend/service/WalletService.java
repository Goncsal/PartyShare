package tqs.backend.tqsbackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tqs.backend.tqsbackend.entity.*;
import tqs.backend.tqsbackend.repository.BookingRepository;
import tqs.backend.tqsbackend.repository.WalletRepository;
import tqs.backend.tqsbackend.repository.WalletTransactionRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class WalletService {

    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final BookingRepository bookingRepository;
    private final UserService userService;

    public WalletService(WalletRepository walletRepository, 
                         WalletTransactionRepository transactionRepository,
                         BookingRepository bookingRepository,
                         UserService userService) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.bookingRepository = bookingRepository;
        this.userService = userService;
    }

    @Transactional
    public Wallet createWallet(Long ownerId) {
        if (walletRepository.existsByOwnerId(ownerId)) {
            logger.warn("Wallet already exists for owner {}", ownerId);
            throw new IllegalStateException("Wallet already exists for this owner");
        }

        Optional<User> userOpt = userService.getUserById(ownerId);
        if (userOpt.isEmpty()) {
            logger.warn("Cannot create wallet: User {} not found", ownerId);
            throw new IllegalArgumentException("User not found");
        }

        User owner = userOpt.get();
        if (owner.getRole() != UserRoles.OWNER) {
            logger.warn("Cannot create wallet: User {} is not an OWNER", ownerId);
            throw new IllegalArgumentException("Only OWNER users can have wallets");
        }

        Wallet wallet = new Wallet(owner);
        wallet = walletRepository.save(wallet);
        logger.info("Created wallet {} for owner {}", wallet.getId(), ownerId);
        return wallet;
    }

    public Optional<Wallet> getWalletByOwnerId(Long ownerId) {
        return walletRepository.findByOwnerId(ownerId);
    }

    @Transactional
    public WalletTransaction holdFunds(Long bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            logger.warn("Cannot hold funds: Booking {} not found", bookingId);
            throw new IllegalArgumentException("Booking not found");
        }

        Booking booking = bookingOpt.get();
        Long ownerId = booking.getItem().getOwnerId();

        // Get or create wallet for owner
        Wallet wallet = walletRepository.findByOwnerId(ownerId)
                .orElseGet(() -> {
                    logger.info("Creating wallet for owner {} on first payment", ownerId);
                    return createWalletInternal(ownerId);
                });

        BigDecimal amount = booking.getTotalPrice();
        wallet.addPendingFunds(amount);
        walletRepository.save(wallet);

        WalletTransaction transaction = new WalletTransaction(wallet, booking, amount);
        transaction = transactionRepository.save(transaction);

        logger.info("Held {} in wallet {} for booking {}", amount, wallet.getId(), bookingId);
        return transaction;
    }

    @Transactional
    public boolean releaseFunds(Long bookingId) {
        Optional<WalletTransaction> txOpt = transactionRepository.findByBookingId(bookingId);
        if (txOpt.isEmpty()) {
            logger.warn("Cannot release funds: Transaction for booking {} not found", bookingId);
            return false;
        }

        WalletTransaction transaction = txOpt.get();
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            logger.warn("Cannot release funds: Transaction {} is not PENDING", transaction.getId());
            return false;
        }

        Booking booking = transaction.getBooking();
        if (!booking.isRenterConfirmed() || !booking.isOwnerConfirmed()) {
            logger.warn("Cannot release funds: Booking {} does not have dual confirmation", bookingId);
            return false;
        }

        Wallet wallet = transaction.getWallet();
        BigDecimal amount = transaction.getAmount();

        wallet.releasePendingFunds(amount);
        walletRepository.save(wallet);

        transaction.release();
        transactionRepository.save(transaction);

        logger.info("Released {} from pending to available for wallet {}", amount, wallet.getId());
        return true;
    }

    @Transactional
    public boolean refundFunds(Long bookingId) {
        Optional<WalletTransaction> txOpt = transactionRepository.findByBookingId(bookingId);
        if (txOpt.isEmpty()) {
            logger.warn("Cannot refund: Transaction for booking {} not found", bookingId);
            return false;
        }

        WalletTransaction transaction = txOpt.get();
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            logger.warn("Cannot refund: Transaction {} is not PENDING", transaction.getId());
            return false;
        }

        Wallet wallet = transaction.getWallet();
        BigDecimal amount = transaction.getAmount();

        wallet.refundPendingFunds(amount);
        walletRepository.save(wallet);

        transaction.refund();
        transactionRepository.save(transaction);

        logger.info("Refunded {} for booking {}", amount, bookingId);
        return true;
    }

    public List<WalletTransaction> getTransactionsByOwnerId(Long ownerId) {
        Optional<Wallet> walletOpt = walletRepository.findByOwnerId(ownerId);
        if (walletOpt.isEmpty()) {
            return List.of();
        }
        return transactionRepository.findByWalletId(walletOpt.get().getId());
    }

    @Transactional
    public BigDecimal withdraw(Long ownerId, BigDecimal amount) {
        Optional<Wallet> walletOpt = walletRepository.findByOwnerId(ownerId);
        if (walletOpt.isEmpty()) {
            logger.warn("Cannot withdraw: Wallet for owner {} not found", ownerId);
            throw new IllegalArgumentException("Wallet not found");
        }

        Wallet wallet = walletOpt.get();
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Cannot withdraw: Invalid amount {}", amount);
            throw new IllegalArgumentException("Amount must be positive");
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            logger.warn("Cannot withdraw: Insufficient balance. Available: {}, Requested: {}", 
                       wallet.getBalance(), amount);
            throw new IllegalArgumentException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        logger.info("Withdrawn {} from wallet {}. New balance: {}", 
                   amount, wallet.getId(), wallet.getBalance());
        
        // In a real implementation, this would trigger a Stripe payout
        // For now, we just reduce the balance (simulating bank transfer)
        return wallet.getBalance();
    }

    @Transactional
    public BigDecimal withdrawAll(Long ownerId) {
        Optional<Wallet> walletOpt = walletRepository.findByOwnerId(ownerId);
        if (walletOpt.isEmpty()) {
            logger.warn("Cannot withdraw: Wallet for owner {} not found", ownerId);
            throw new IllegalArgumentException("Wallet not found");
        }

        Wallet wallet = walletOpt.get();
        BigDecimal amount = wallet.getBalance();
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Cannot withdraw: No available balance");
            throw new IllegalArgumentException("No available balance to withdraw");
        }

        wallet.setBalance(BigDecimal.ZERO);
        walletRepository.save(wallet);

        logger.info("Withdrawn all {} from wallet {}", amount, wallet.getId());
        return amount;
    }

    private Wallet createWalletInternal(Long ownerId) {
        Optional<User> userOpt = userService.getUserById(ownerId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        Wallet wallet = new Wallet(userOpt.get());
        return walletRepository.save(wallet);
    }
}
