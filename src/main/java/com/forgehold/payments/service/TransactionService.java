
package com.forgehold.payments.service;

import com.forgehold.payments.entity.Transaction;
import com.forgehold.payments.entity.Wallet;
import com.forgehold.payments.repository.TransactionRepository;
import com.forgehold.payments.repository.WalletRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(
            WalletRepository walletRepository,
            TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Transaction transfer(
            Long senderWalletId,
            Long receiverWalletId,
            BigDecimal amount) {

        if (senderWalletId == null || receiverWalletId == null
                || senderWalletId.equals(receiverWalletId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sender and receiver wallets must be different."
            );
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0
                || amount.scale() > 2) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Amount must be positive and have at most two decimal places."
            );
        }

        // Lock wallets in ID order to reduce deadlock risk.
        Long firstId = Math.min(senderWalletId, receiverWalletId);
        Long secondId = Math.max(senderWalletId, receiverWalletId);

        Wallet firstWallet = walletRepository.findByIdForUpdate(firstId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Wallet not found."));

        Wallet secondWallet = walletRepository.findByIdForUpdate(secondId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Wallet not found."));

        Wallet sender = senderWalletId.equals(firstId)
                ? firstWallet : secondWallet;

        Wallet receiver = receiverWalletId.equals(firstId)
                ? firstWallet : secondWallet;

        if (!sender.getCurrency().equals(receiver.getCurrency())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Wallet currencies must match."
            );
        }

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Insufficient wallet balance."
            );
        }

        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        walletRepository.save(sender);
        walletRepository.save(receiver);

        Transaction transaction = new Transaction();
        transaction.setReference(UUID.randomUUID().toString());
        transaction.setSenderWalletId(senderWalletId);
        transaction.setReceiverWalletId(receiverWalletId);
        transaction.setAmount(amount);
        transaction.setCurrency(sender.getCurrency());
        transaction.setStatus("COMPLETED");

        return transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getWalletTransactions(Long walletId) {

        if (!walletRepository.existsById(walletId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Wallet not found."
            );
        }

        return transactionRepository
                .findBySenderWalletIdOrReceiverWalletIdOrderByCreatedAtDesc(
                        walletId, walletId);
    }
}
