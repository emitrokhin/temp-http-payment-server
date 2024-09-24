package ru.emitrohin.paymentserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.emitrohin.paymentserver.model.Transaction;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionId(long transactionId);
}