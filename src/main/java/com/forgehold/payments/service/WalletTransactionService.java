
package com.forgehold.payments.service;

import com.forgehold.payments.entity.Wallet;
import com.forgehold.payments.entity.WalletTransaction;
import com.forgehold.payments.repository.WalletRepository;
import com.forgehold.payments.repository.WalletTransactionRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WalletTransactionService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    public WalletTransactionService(
            WalletRepository walletRepository,
            WalletTransactionRepository transactionRepository) {

        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public WalletTransaction deposit(Long walletId, BigDecimal amount) {

        validateAmount(amount);

        Wallet wallet = getWalletForUpdate(walletId);

        BigDecimal newBalance = wallet.getBalance().add(amount);

        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setType(
                WalletTransaction.TransactionType.DEPOSIT
        );
        transaction.setAmount(amount);
        transaction.setBalanceAfter(newBalance);
        transaction.setStatus(
                WalletTransaction.TransactionStatus.COMPLETED
        );
        transaction.setReference(UUID.randomUUID().toString());

        return transactionRepository.save(transaction);
    }

    @Transactional
    public WalletTransaction withdraw(Long walletId, BigDecimal amount) {

        validateAmount(amount);

        Wallet wallet = getWalletForUpdate(walletId);

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Insufficient wallet balance."
            );
        }

        BigDecimal newBalance = wallet.getBalance().subtract(amount);

        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setType(
                WalletTransaction.TransactionType.WITHDRAWAL
        );
        transaction.setAmount(amount);
        transaction.setBalanceAfter(newBalance);
        transaction.setStatus(
                WalletTransaction.TransactionStatus.COMPLETED
        );
        transaction.setReference(UUID.randomUUID().toString());

        return transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public Page<WalletTransaction> getWalletTransactions(
            Long walletId,
            int page,
            int size) {

        if (!walletRepository.existsById(walletId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Wallet not found."
            );
        }

        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page number cannot be negative."
            );
        }

        if (size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page size must be between 1 and 100."
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return transactionRepository
                .findByWalletIdOrderByCreatedAtDesc(
                        walletId,
                        pageable
                );
    }

    private Wallet getWalletForUpdate(Long walletId) {

        return walletRepository.findByIdForUpdate(walletId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Wallet not found."
                ));
    }

    private void validateAmount(BigDecimal amount) {

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0
                || amount.scale() > 2) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Amount must be greater than zero and have at most two decimal places."
            );
        }
    }
}
