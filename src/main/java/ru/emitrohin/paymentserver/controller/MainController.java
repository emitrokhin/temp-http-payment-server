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

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/subscribe")
    public String subscribe(Model model) {
        var telegramId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        if (properties.ownerId() != telegramId && !canPayForSubscription()) {
            return "redirect:/payment-unavailable";
        }

        // проверка или создание подписки
        if (subscriptionService.hasPaidSubscription(telegramId)) {
            return "redirect:/success";
        };

        var profilePaymentForm = profileService.findByTelegramId(telegramId)
                .map(mapper::createPaymentResponse)
                .orElseGet(() -> new ProfilePaymentDTO("","","",""));

        model.addAttribute("profilePaymentForm", profilePaymentForm);

        return "subscribe";
    }

    @GetMapping("/first-run")
    public String firstRun(@PathParam("step") String step, Model model) {

        var telegramId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        if (firstRunService.findFirstRun(telegramId).isPresent()) {
            return "redirect:/";
        }

        if (!StringUtils.hasLength(step)) {
            return "first-run";
        } else if (step.equals("last-step")) {
            return "first-run-last-step";
        }

        return "first-run";
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
