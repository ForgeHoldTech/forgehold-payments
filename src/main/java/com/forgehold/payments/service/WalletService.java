
package com.forgehold.payments.service;

import com.forgehold.payments.entity.Customer;
import com.forgehold.payments.entity.Wallet;
import com.forgehold.payments.repository.CustomerRepository;
import com.forgehold.payments.repository.WalletRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final CustomerRepository customerRepository;

    public WalletService(
            WalletRepository walletRepository,
            CustomerRepository customerRepository) {
        this.walletRepository = walletRepository;
        this.customerRepository = customerRepository;
    }

    // Create a wallet for an existing customer
    public Wallet createWallet(Long customerId, String currency) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Customer not found."
                ));

        String normalizedCurrency = currency.trim().toUpperCase();

        if (!normalizedCurrency.matches("[A-Z]{3}")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Currency must be a valid three-letter code."
            );
        }

        if (walletRepository.findByCustomerIdAndCurrency(
                customerId, normalizedCurrency).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This customer already has a wallet in this currency."
            );
        }

        Wallet wallet = new Wallet();
        wallet.setCustomer(customer);
        wallet.setCurrency(normalizedCurrency);
        wallet.setBalance(BigDecimal.ZERO);

        return walletRepository.save(wallet);
    }

    // Get all wallets belonging to a customer
    public List<Wallet> getCustomerWallets(Long customerId) {

        if (!customerRepository.existsById(customerId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Customer not found."
            );
        }

        return walletRepository.findByCustomerId(customerId);
    }
}
