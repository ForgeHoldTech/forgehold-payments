
package com.forgehold.payments.controller;

import com.forgehold.payments.entity.Wallet;
import com.forgehold.payments.service.WalletService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    public record CreateWalletRequest(
        @NotBlank
        @Pattern(
            regexp = "[A-Za-z]{3}",
            message = "Currency must be a three-letter code."
        )
        String currency
    ) {}

    // Response DTO: send only the required wallet details
    public record WalletResponse(
        Long id,
        Long customerId,
        String currency,
        java.math.BigDecimal balance,
        java.time.LocalDateTime createdAt
    ) {}

    // Create a wallet for a customer
    @PostMapping("/customer/{customerId}")
    public ResponseEntity<WalletResponse> createWallet(
            @PathVariable Long customerId,
            @Valid @RequestBody CreateWalletRequest request) {

        Wallet wallet = walletService.createWallet(
                customerId,
                request.currency()
        );

        WalletResponse response = toResponse(wallet);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get all wallets belonging to a customer
    
    @GetMapping("/customer/{customerId}")
    public List<WalletResponse> getCustomerWallets(
            @PathVariable Long customerId) {

        return walletService.getCustomerWallets(customerId)
                .stream()
                .map(WalletController::toResponse)
                .toList();
    }


    // Convert entity into a safe JSON response
    private static WalletResponse toResponse(Wallet wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getCustomer().getId(),
                wallet.getCurrency(),
                wallet.getBalance(),
                wallet.getCreatedAt()
        );
    }
}
