package ru.emitrohin.paymentserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.emitrohin.paymentserver.dto.mapper.ProfileMapper;
import ru.emitrohin.paymentserver.dto.TransactionResponse;
import ru.emitrohin.paymentserver.dto.mapper.TransactionMapper;
import ru.emitrohin.paymentserver.service.FirstRunService;
import ru.emitrohin.paymentserver.service.ProfileService;
import ru.emitrohin.paymentserver.service.TransactionService;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final FirstRunService firstRunService;
    private final ProfileMapper profileMapper;
    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    @GetMapping("/profile")
    public String getUser(Model model) {
        var telegramId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        // Получение данных профиля
        var profileForm = profileService.findByTelegramId(telegramId);
        if (profileForm.isEmpty()) {
            return "redirect:/";
        }

        // Получение списка транзакций и маппинг с использованием TransactionMapper
        var transactionDTOs = transactionService.getAllTransactions(telegramId)
                .stream()
                .map(transactionMapper::toTransactionResponse)
                .collect(Collectors.toList());

        // Передача данных в модель
        var firstRunEntry = firstRunService.findFirstRun(telegramId);
        model.addAttribute("firstRun", firstRunEntry.isEmpty());
        model.addAttribute("profileForm", profileMapper.createUpdateResponse(profileForm.get()));
        model.addAttribute("transactions", transactionDTOs);

        return "profile";
    }
}
