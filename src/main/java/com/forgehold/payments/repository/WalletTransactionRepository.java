
package com.forgehold.payments.repository;

import com.forgehold.payments.entity.WalletTransaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletTransactionRepository
        extends JpaRepository<WalletTransaction, Long> {

    Page<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(
            Long walletId,
            Pageable pageable
    );

    Optional<WalletTransaction> findByReference(String reference);

    boolean existsByReference(String reference);
}
