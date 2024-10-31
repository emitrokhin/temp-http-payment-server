package ru.emitrohin.paymentserver.controller;

import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import ru.emitrohin.paymentserver.config.TelegramProperties;
import ru.emitrohin.paymentserver.dto.mapper.ProfileMapper;
import ru.emitrohin.paymentserver.dto.profile.ProfilePaymentDTO;
import ru.emitrohin.paymentserver.service.CardService;
import ru.emitrohin.paymentserver.service.FirstRunService;
import ru.emitrohin.paymentserver.service.ProfileService;
import ru.emitrohin.paymentserver.service.SubscriptionService;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@EnableConfigurationProperties(TelegramProperties.class)
public class MainController {

    private final ProfileService profileService;

    private final FirstRunService firstRunService;

    private final ProfileMapper mapper;

    private final SubscriptionService subscriptionService;

    private final TelegramProperties properties;

    private final CardService cardService;

    @GetMapping("/")
    public String index() {
        var telegramId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        if (subscriptionService.hasPaidSubscription(telegramId)) {
            return "redirect:/success";
        };

        return "index";
    }

    @GetMapping("/subscribe")
    public String subscribe(Model model) {
        var telegramId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        // проверка или создание подписки
        if (subscriptionService.hasPaidSubscription(telegramId)) {
            return "redirect:/success";
        };

        if (properties.ownerId() != telegramId && !canPayForSubscription()) {
            return "redirect:/payment-unavailable";
        }

        var profilePaymentForm = profileService.findByTelegramId(telegramId)
                .map(mapper::createPaymentResponse)
                .orElseGet(() -> new ProfilePaymentDTO("","","","", telegramId));

        var primaryCard = cardService.getPrimaryCard(telegramId);

        model.addAttribute("profilePaymentForm", profilePaymentForm);
        model.addAttribute("primaryCard", primaryCard);

        return "subscribe";
    }

    @GetMapping("/onboarding")
    public String firstRun(@PathParam("step") String step) {
        var telegramId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        if (!StringUtils.hasLength(step)) {
            return "onboarding";
        } else if (step.equals("last-step")) {
            if (firstRunService.findFirstRun(telegramId).isEmpty()) {
                firstRunService.completeFirstRun(telegramId);
                return "onboarding-last-step";
            } else {
                return "redirect:/success";
            }
        }

        return "onboarding";
    }

    @GetMapping("/unavailable")
    public String unavailable() {
        return "unavailable";
    }

    @GetMapping("/unsupported")
    public String unsupported() {
        return "unsupported";
    }

    @GetMapping("/payment-unavailable")
    public String paymentUnavailable() {
        return "payment-unavailable";
    }

    @GetMapping("/auth")
    public String auth() {
        return "auth";
    }

    public boolean canPayForSubscription() {
        // Проверяем, находится ли текущая дата между 1 и 3 числами месяца
        return LocalDate.now().getDayOfMonth() <= 3;
    }

}