package tqs.backend.tqsbackend.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tqs.backend.tqsbackend.dto.WalletDTO;
import tqs.backend.tqsbackend.dto.WalletTransactionDTO;
import tqs.backend.tqsbackend.entity.Wallet;
import tqs.backend.tqsbackend.entity.WalletTransaction;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.service.WalletService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public ResponseEntity<WalletDTO> getWallet(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        UserRoles role = (UserRoles) session.getAttribute("userRole");

        if (userId == null || role != UserRoles.OWNER) {
            return ResponseEntity.status(403).build();
        }

        Optional<Wallet> walletOpt = walletService.getWalletByOwnerId(userId);
        if (walletOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Wallet wallet = walletOpt.get();
        WalletDTO dto = new WalletDTO(
                wallet.getId(),
                wallet.getBalance(),
                wallet.getPendingBalance(),
                wallet.getCreatedAt()
        );
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<WalletTransactionDTO>> getTransactions(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        UserRoles role = (UserRoles) session.getAttribute("userRole");

        if (userId == null || role != UserRoles.OWNER) {
            return ResponseEntity.status(403).build();
        }

        List<WalletTransaction> transactions = walletService.getTransactionsByOwnerId(userId);
        List<WalletTransactionDTO> dtos = transactions.stream()
                .map(tx -> new WalletTransactionDTO(
                        tx.getId(),
                        tx.getBooking().getId(),
                        tx.getAmount(),
                        tx.getStatus().name(),
                        tx.getCreatedAt(),
                        tx.getReleasedAt()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<WithdrawResponse> withdraw(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        UserRoles role = (UserRoles) session.getAttribute("userRole");

        if (userId == null || role != UserRoles.OWNER) {
            return ResponseEntity.status(403).build();
        }

        try {
            java.math.BigDecimal withdrawnAmount = walletService.withdrawAll(userId);
            return ResponseEntity.ok(new WithdrawResponse(
                    true, 
                    "Successfully withdrawn €" + withdrawnAmount,
                    withdrawnAmount,
                    java.math.BigDecimal.ZERO
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new WithdrawResponse(
                    false,
                    e.getMessage(),
                    java.math.BigDecimal.ZERO,
                    null
            ));
        }
    }

    public record WithdrawResponse(
            boolean success,
            String message,
            java.math.BigDecimal withdrawnAmount,
            java.math.BigDecimal newBalance
    ) {}
}
