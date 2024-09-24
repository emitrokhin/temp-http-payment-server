package ru.emitrohin.paymentserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.emitrohin.paymentserver.dto.PersonalDataPaymentResponse;
import ru.emitrohin.paymentserver.dto.mapper.PersonalDataMapper;
import ru.emitrohin.paymentserver.service.PersonalDataService;
import ru.emitrohin.paymentserver.service.SubscriptionService;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final PersonalDataService service;

    private final PersonalDataMapper mapper;

    private final SubscriptionService subscriptionService;

    @GetMapping("/")
    public String index(Model model) {
        var telegramId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        // проверка или создание подписки
        if (subscriptionService.hasPaidSubscription(telegramId)) {
            return "success";
        };

        var personalDataPayment = service.findByTelegramId(telegramId).map(mapper::createResponse).orElseGet(PersonalDataPaymentResponse::new);
        model.addAttribute("personalDataPayment", personalDataPayment);
        return "index";
    }

    @GetMapping("/unavailable")
    public String unavailable() {
        return "unavailable";
    }

    @GetMapping("/unsupported")
    public String unsupported() {
        return "unsupported";
    }

    @GetMapping("/auth")
    public String auth() {
        return "auth";
    }
}
