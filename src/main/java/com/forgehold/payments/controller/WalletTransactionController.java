
package com.forgehold.payments.controller;

import com.forgehold.payments.entity.WalletTransaction;
import com.forgehold.payments.service.WalletTransactionService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class WalletTransactionController {

    private final WalletTransactionService transactionService;

    public WalletTransactionController(
            WalletTransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public record TransactionRequest(
        @NotNull
        @DecimalMin(
            value = "0.00",
            inclusive = false,
            message = "Amount must be greater than zero."
        )
        @Digits(
            integer = 17,
            fraction = 2,
            message = "Amount can have at most two decimal places."
        )
        BigDecimal amount
    ) {}

    public record TransactionResponse(
        Long id,
        Long walletId,
        String type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String status,
        String reference,
        LocalDateTime createdAt
    ) {}

    public record TransactionPageResponse(
        List<TransactionResponse> transactions,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
    ) {}

    @PostMapping("/wallet/{walletId}/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @PathVariable Long walletId,
            @Valid @RequestBody TransactionRequest request) {

        WalletTransaction transaction = transactionService.deposit(
                walletId,
                request.amount()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(transaction));
    }

    @PostMapping("/wallet/{walletId}/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @PathVariable Long walletId,
            @Valid @RequestBody TransactionRequest request) {

        WalletTransaction transaction = transactionService.withdraw(
                walletId,
                request.amount()
        );

        return ResponseEntity.ok(toResponse(transaction));
    }

    @GetMapping("/wallet/{walletId}")
    public TransactionPageResponse getWalletTransactions(
            @PathVariable Long walletId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<WalletTransaction> result =
                transactionService.getWalletTransactions(
                        walletId,
                        page,
                        size
                );

        List<TransactionResponse> transactions =
                result.getContent()
                        .stream()
                        .map(WalletTransactionController::toResponse)
                        .toList();

        return new TransactionPageResponse(
                transactions,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        );
    }

    private static TransactionResponse toResponse(
            WalletTransaction transaction) {

        return new TransactionResponse(
                transaction.getId(),
                transaction.getWallet().getId(),
                transaction.getType().name(),
                transaction.getAmount(),
                transaction.getBalanceAfter(),
                transaction.getStatus().name(),
                transaction.getReference(),
                transaction.getCreatedAt()
        );
    }
}
