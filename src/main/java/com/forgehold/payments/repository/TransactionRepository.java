
package com.forgehold.payments.repository;

import com.forgehold.payments.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByReference(String reference);

    List<Transaction> findBySenderWalletIdOrReceiverWalletIdOrderByCreatedAtDesc(
            Long senderWalletId,
            Long receiverWalletId
    );
}
