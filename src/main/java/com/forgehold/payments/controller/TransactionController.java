
package com.forgehold.payments.controller;

import com.forgehold.payments.entity.Transaction;
import com.forgehold.payments.service.TransactionService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public record TransferRequest(
            @NotNull @Positive Long senderWalletId,
            @NotNull @Positive Long receiverWalletId,
            @NotNull
            @DecimalMin(value = "0.01")
            @Digits(integer = 17, fraction = 2)
            BigDecimal amount
    ) {}

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction transfer(
            @Valid @RequestBody TransferRequest request) {

        return transactionService.transfer(
                request.senderWalletId(),
                request.receiverWalletId(),
                request.amount()
        );
    }
}
