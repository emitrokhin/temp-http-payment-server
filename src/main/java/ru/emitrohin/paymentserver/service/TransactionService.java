package ru.emitrohin.paymentserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.emitrohin.paymentserver.model.Transaction;
import ru.emitrohin.paymentserver.repository.TransactionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public void save(Transaction entity) {
        transactionRepository.save(entity);
    }

    public List<Transaction> getAllTransactions(long telegramId) {
        return transactionRepository.findAllByTelegramId(telegramId);
    }
}
