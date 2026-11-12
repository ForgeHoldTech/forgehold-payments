
package com.forgehold.payments.service;

import com.forgehold.payments.entity.Wallet;
import com.forgehold.payments.entity.WalletTransaction;
import com.forgehold.payments.repository.WalletRepository;
import com.forgehold.payments.repository.WalletTransactionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletTransactionServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository transactionRepository;

    @InjectMocks
    private WalletTransactionService transactionService;

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        wallet = new Wallet();
        wallet.setBalance(new BigDecimal("10000.00"));
    }

    private void prepareWallet() {
        when(walletRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(wallet));
    }

    private void prepareTransactionSave() {
        when(transactionRepository.save(any(WalletTransaction.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));
    }

    @Test
    void depositShouldIncreaseWalletBalance() {

        prepareWallet();
        prepareTransactionSave();

        WalletTransaction transaction =
                transactionService.deposit(
                        1L,
                        new BigDecimal("2000.00")
                );

        assertEquals(
                0,
                new BigDecimal("12000.00")
                        .compareTo(wallet.getBalance())
        );

        assertNotNull(transaction);
        assertEquals(
                WalletTransaction.TransactionType.DEPOSIT,
                transaction.getType()
        );
        assertEquals(
                WalletTransaction.TransactionStatus.COMPLETED,
                transaction.getStatus()
        );

        verify(walletRepository).save(wallet);
        verify(transactionRepository).save(any(WalletTransaction.class));
    }

    @Test
    void withdrawalShouldDecreaseWalletBalance() {

        prepareWallet();
        prepareTransactionSave();

        WalletTransaction transaction =
                transactionService.withdraw(
                        1L,
                        new BigDecimal("3000.00")
                );

        assertEquals(
                0,
                new BigDecimal("7000.00")
                        .compareTo(wallet.getBalance())
        );

        assertNotNull(transaction);
        assertEquals(
                WalletTransaction.TransactionType.WITHDRAWAL,
                transaction.getType()
        );
        assertEquals(
                WalletTransaction.TransactionStatus.COMPLETED,
                transaction.getStatus()
        );

        verify(walletRepository).save(wallet);
        verify(transactionRepository).save(any(WalletTransaction.class));
    }

    @Test
    void withdrawalShouldFailWhenBalanceIsInsufficient() {

        prepareWallet();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transactionService.withdraw(
                        1L,
                        new BigDecimal("15000.00")
                )
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        assertEquals(
                0,
                new BigDecimal("10000.00")
                        .compareTo(wallet.getBalance())
        );

        verify(walletRepository, never()).save(any(Wallet.class));
        verify(transactionRepository, never())
                .save(any(WalletTransaction.class));
    }

    @Test
    void depositShouldRejectZeroAmount() {

        assertThrows(
                ResponseStatusException.class,
                () -> transactionService.deposit(
                        1L,
                        BigDecimal.ZERO
                )
        );

        verify(walletRepository, never())
                .findByIdForUpdate(anyLong());
        verify(walletRepository, never()).save(any(Wallet.class));
        verify(transactionRepository, never())
                .save(any(WalletTransaction.class));
    }

    @Test
    void depositShouldRejectNegativeAmount() {

        assertThrows(
                ResponseStatusException.class,
                () -> transactionService.deposit(
                        1L,
                        new BigDecimal("-100.00")
                )
        );

        verify(walletRepository, never())
                .findByIdForUpdate(anyLong());
        verify(walletRepository, never()).save(any(Wallet.class));
        verify(transactionRepository, never())
                .save(any(WalletTransaction.class));
    }

    @Test
    void depositShouldRejectAmountWithMoreThanTwoDecimalPlaces() {

        assertThrows(
                ResponseStatusException.class,
                () -> transactionService.deposit(
                        1L,
                        new BigDecimal("100.123")
                )
        );

        verify(walletRepository, never())
                .findByIdForUpdate(anyLong());
        verify(walletRepository, never()).save(any(Wallet.class));
        verify(transactionRepository, never())
                .save(any(WalletTransaction.class));
    }

    @Test
    void withdrawalShouldRejectAmountWithMoreThanTwoDecimalPlaces() {

        assertThrows(
                ResponseStatusException.class,
                () -> transactionService.withdraw(
                        1L,
                        new BigDecimal("50.999")
                )
        );

        verify(walletRepository, never())
                .findByIdForUpdate(anyLong());
        verify(walletRepository, never()).save(any(Wallet.class));
        verify(transactionRepository, never())
                .save(any(WalletTransaction.class));
    }

    @Test
    void depositShouldFailWhenWalletDoesNotExist() {

        when(walletRepository.findByIdForUpdate(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transactionService.deposit(
                        99L,
                        new BigDecimal("500.00")
                )
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(walletRepository, never()).save(any(Wallet.class));
        verify(transactionRepository, never())
                .save(any(WalletTransaction.class));
    }
}
