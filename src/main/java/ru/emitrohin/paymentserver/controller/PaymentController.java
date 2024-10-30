package ru.emitrohin.paymentserver.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import ru.emitrohin.paymentserver.config.CloudpaymentsProperties;
import ru.emitrohin.paymentserver.dto.cloudpayments.CloudpaymentsRequest;
import ru.emitrohin.paymentserver.dto.profile.ProfilePaymentDTO;
import ru.emitrohin.paymentserver.model.PaymentStatus;
import ru.emitrohin.paymentserver.service.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    private final PaymentService paymentService;

    private final RestTemplate restTemplate;

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

            var payment = paymentService.createPayment(telegramId);
            model.addAttribute("paymentId", payment.getId());
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

    @PostMapping("/tokens/charge")
    public ResponseEntity<?> chargeToken(@RequestBody CloudpaymentsRequest request) {
        var telegramId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        var publicKey = property.publicKey();
        var apiSecret = property.password();

        var cloudPaymentsApiUrl = "https://api.cloudpayments.ru/payments/tokens/charge";

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        var credentials = publicKey + ":" + apiSecret;
        var base64Credentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        headers.set("Authorization", "Basic " + base64Credentials);

        var requestBody = new HashMap<>();
        requestBody.put("Amount", request.getAmount());
        requestBody.put("Currency", request.getCurrency());
        requestBody.put("AccountId", telegramId);
        requestBody.put("TrInitiatorCode", 1);
        requestBody.put("Token", request.getToken());
        requestBody.put("Description", "Оплата подписки");

        var entity = new HttpEntity<>(requestBody, headers);

        paymentService.createPayment(telegramId);

        var response = restTemplate.postForEntity(cloudPaymentsApiUrl, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            logger.info(telegramId + " successfully paid with card " + request.getToken());
            return ResponseEntity.ok().build();
        } else {
            logger.info(telegramId + " failed to paid with card " + request.getToken());
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        }
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
        var telegramId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        var payment = paymentService.getLastPendingPayment(telegramId);
        paymentService.updatePaymentStatus(payment.get().getId(), PaymentStatus.FAILED);
        return "fail";
    }

    @GetMapping("/payment/status")
    public ResponseEntity<String> getPaymentStatus() {
        var telegramId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        var payment = paymentService.getLastPendingPayment(telegramId);
        return payment.isPresent()
                ? ResponseEntity.ok(String.valueOf(payment.get().getPaymentStatus()))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Attempt not found");
    }

}
