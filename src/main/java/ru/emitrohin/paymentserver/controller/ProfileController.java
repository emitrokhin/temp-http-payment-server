package ru.emitrohin.paymentserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.emitrohin.paymentserver.dto.mapper.CardMapper;
import ru.emitrohin.paymentserver.dto.mapper.ProfileMapper;
import ru.emitrohin.paymentserver.dto.mapper.TransactionMapper;
import ru.emitrohin.paymentserver.service.CardService;
import ru.emitrohin.paymentserver.service.FirstRunService;
import ru.emitrohin.paymentserver.service.ProfileService;
import ru.emitrohin.paymentserver.service.TransactionService;


@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final FirstRunService firstRunService;
    private final ProfileMapper profileMapper;
    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;
    private final CardService cardService;
    private final CardMapper cardMapper;

    @GetMapping("/profile")
    public String getUser(Model model) {
        var telegramId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        // Получение данных профиля
        var profileForm = profileService.findByTelegramId(telegramId);
        if (profileForm.isEmpty()) {
            return "redirect:/";
        }

        // Получение списка транзакций, маппинг и сортировка по дате от самой свежей к самой старой
        var transactions = transactionService.getAllTransactions(telegramId)
                .stream()
                .map(transactionMapper::toTransactionResponse)
                .sorted((t1, t2) -> t2.dateTime().compareTo(t1.dateTime())) // Сортировка от новой к старой
                .toList();
        var cards = cardService.getAllCards(telegramId)
                .stream()
                .map(cardMapper::toCardResponse)
                .toList();

        // Передача данных в модель
        var firstRunEntry = firstRunService.findFirstRun(telegramId);
        model.addAttribute("firstRun", firstRunEntry.isEmpty());
        model.addAttribute("profileForm", profileMapper.createUpdateResponse(profileForm.get()));
        model.addAttribute("transactions", transactions);
        model.addAttribute("cards", cards);

        return "profile";
    }

}
