package ru.emitrohin.paymentserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.emitrohin.paymentserver.dto.TransactionDTO;
import ru.emitrohin.paymentserver.model.Transaction;
import ru.emitrohin.paymentserver.service.ProfileService;
import ru.emitrohin.paymentserver.service.TransactionService;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    private final ProfileService profileService;

    @GetMapping("/transactions")
    @ResponseBody
    public List<TransactionDTO> getAllTransactions() {
        var telegramId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        var profile = profileService.findByTelegramId(telegramId);
        List<Transaction> transactions = transactionService.getAllTransactions(telegramId);

        return transactions.stream().map(transaction -> {
            TransactionDTO dto = new TransactionDTO();
            dto.setFirstName(profile.get().getFirstName());
            dto.setLastName(profile.get().getLastName());
            dto.setAmount(transaction.getAmount());
            dto.setDateTime(transaction.getDateTime());
            dto.setCurrency(transaction.getCurrency());
            return dto;
        }).collect(Collectors.toList());
    }
}
