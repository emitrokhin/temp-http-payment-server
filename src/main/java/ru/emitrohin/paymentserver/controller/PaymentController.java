package ru.emitrohin.paymentserver.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.emitrohin.paymentserver.config.CloudpaymentsProperties;
import ru.emitrohin.paymentserver.dto.profile.ProfilePaymentDTO;
import ru.emitrohin.paymentserver.service.FirstRunService;
import ru.emitrohin.paymentserver.service.ProfileService;
import ru.emitrohin.paymentserver.service.SubscriptionService;
import ru.emitrohin.paymentserver.service.TelegramUserDataService;

@Controller
@RequiredArgsConstructor
@EnableConfigurationProperties(CloudpaymentsProperties.class)
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final TelegramUserDataService telegramUserDataService;

    private final ProfileService profileService;

    private final SubscriptionService subscriptionService;

    private final FirstRunService firstRunService;

    private final CloudpaymentsProperties property;

    //TODO учет стартовавших оплату не сделавших, чтобы сообщить ботом, что надо оплатить
    @PostMapping("/pay")
    public String pay(@Valid @ModelAttribute("profilePaymentForm") ProfilePaymentDTO updateRequest, BindingResult bindingResult, Model model) {
        var telegramId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        // есть ли такой пользователь?
        var userData = telegramUserDataService.findByTelegramId(telegramId);
        if (userData.isEmpty()) {
            logger.error("User with id {} not found", telegramId);
            bindingResult.rejectValue("telegramId", "error.personalDataForm", "Пользователь с таким Telegram ID не найден");
            return "redirect:/index";
        } else {
            // есть ли ошибки?
            if (bindingResult.hasErrors()) {
                logger.error("Binding result has errors {}", bindingResult.getAllErrors());
                return "redirect:/index";
            }
            // оплачена ли подписка?
            if (subscriptionService.hasPaidSubscription(telegramId)) {
                return "redirect:/success";
            }

            // создана ли подписка ?
            var subscription = subscriptionService.findCurrentSubscription(telegramId);
            if (subscription.isEmpty()) {
                subscriptionService.createPendingSubscription(telegramId);
            }

            profileService.saveOrUpdateProfilePayment(telegramId, updateRequest);
        }

        model.addAttribute("telegramId", telegramId);
        model.addAttribute("publicKeyId", property.publicKey());
        model.addAttribute("firstName", updateRequest.firstName());
        model.addAttribute("lastName", updateRequest.lastName());
        model.addAttribute("phone", updateRequest.phone());
        model.addAttribute("email", updateRequest.email());
        return "pay";
    }

    @GetMapping("/success")
    public String success(Model model) {
        var telegramId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        var firstRunEntry = firstRunService.findFirstRun(telegramId);
        model.addAttribute("firstRun", firstRunEntry.isEmpty());
        return "success";
    }

    //TODO показать код ошибки и причину. Спросить у разрабов cloudpayments
    @GetMapping("/fail")
    public String fail() {
        return "fail";
    }
}
