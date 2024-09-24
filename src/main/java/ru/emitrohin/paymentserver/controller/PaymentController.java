package ru.emitrohin.paymentserver.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.emitrohin.paymentserver.config.PublicKeyProperty;
import ru.emitrohin.paymentserver.dto.FailedPaymentRequest;
import ru.emitrohin.paymentserver.dto.PersonalDataPaymentRequest;
import ru.emitrohin.paymentserver.service.PersonalDataService;
import ru.emitrohin.paymentserver.service.SubscriptionService;
import ru.emitrohin.paymentserver.service.TelegramUserDataService;

@Controller
@RequiredArgsConstructor
@EnableConfigurationProperties(PublicKeyProperty.class)
public class PaymentController {

    private final TelegramUserDataService telegramUserDataService;

    private final PersonalDataService personalDataService;

    private final SubscriptionService subscriptionService;

    private final PublicKeyProperty property;

    //TODO учет стартовавших оплату не сделавших
    @PostMapping("/pay")
    public String pay(@Valid @ModelAttribute("personalDataPayment") PersonalDataPaymentRequest personalDataPaymentRequest, BindingResult bindingResult, Model model) {
        var telegramId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        // есть ли такой пользователь?
        var userData = telegramUserDataService.findByTelegramId(telegramId);
        if (userData.isEmpty()) {
            bindingResult.rejectValue("telegramId", "error.personalDataForm", "Пользователь с таким Telegram ID не найден");
            return "index";
        } else {
            // есть ли ошибки?
            if (bindingResult.hasErrors()) {
                return "index";
            }
            // оплачена ли подписка?
            if (subscriptionService.hasPaidSubscription(telegramId)) {
                return "success";
            }

            // создана ли подписка ?
            var subscription = subscriptionService.findCurrentSubscription(telegramId);
            if (subscription.isEmpty()) {
                subscriptionService.createPendingSubscription(telegramId);
            }

            personalDataService.saveOrUpdate(telegramId, personalDataPaymentRequest);
        }

        model.addAttribute("telegramId", telegramId);
        model.addAttribute("publicKeyId", property.publicKey());
        model.addAttribute("firstName", personalDataPaymentRequest.getFirstName());
        model.addAttribute("lastName", personalDataPaymentRequest.getLastName());
        model.addAttribute("phone", personalDataPaymentRequest.getPhone());
        model.addAttribute("email", personalDataPaymentRequest.getEmail());
        return "pay";
    }

    @GetMapping("/success")
    public String success() {
        return "success";
    }

    //TODO показать код ошибки и причину. Спросить у разрабов cloudpayments
    @GetMapping("/fail")
    public String fail() {
        return "fail";
    }
}
