package ru.emitrohin.paymentserver.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.emitrohin.paymentserver.client.BotMotherClient;
import ru.emitrohin.paymentserver.client.TelegramBotClient;
import ru.emitrohin.paymentserver.dto.cloudpayments.CloudpaymentsRequest;
import ru.emitrohin.paymentserver.dto.mapper.TransactionMapper;
import ru.emitrohin.paymentserver.model.SubscriptionStatus;
import ru.emitrohin.paymentserver.service.SubscriptionService;
import ru.emitrohin.paymentserver.service.TransactionService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
//TODO change to api
//TODO client retries and error handling
public class CloudpaymentsWebhookController {

    private final Logger logger = LoggerFactory.getLogger(CloudpaymentsWebhookController.class);

    private final TelegramBotClient telegramBotClient;

    private final BotMotherClient botMotherClient;

    private final TransactionService transactionService;

    private final SubscriptionService subscriptionService;

    private final TransactionMapper mapper;

    //TODO check webhook повторный платеж и проверка accountId
    @PostMapping( "/cloudpayments/success")
    public ResponseEntity<Map<String, Integer>> successWebhook(@Valid CloudpaymentsRequest request) {

        //todo это временное решение, должно работать в связке с check уведомлением
        try {
            var telegramId = Long.parseLong(request.getAccountId());
            var entity = mapper.createFromRequest(request);
            transactionService.save(entity);
            subscriptionService.createOrUpdateCurrentSubscriptionStatus(telegramId, SubscriptionStatus.PAID);
            //TODO пользователь может запретить писать себе
            telegramBotClient.sendMessageWithButtons("Твоя подписка успешно оплачена! 🎉\n\nВот ссылки для твоего удобства \uD83D\uDC47", telegramId);
            botMotherClient.sendPayload(telegramId);
        } catch (NumberFormatException e) {
            logger.error("AccountId {} should be numeric. Can't save to DB", request.getAccountId());
        }

        var response = new HashMap<String, Integer>();
        response.put("code", 0);
        return ResponseEntity.ok(response);
    }

    @PostMapping( "/cloudpayments/fail")
    public ResponseEntity<Map<String, Integer>> failWebhook(@Valid CloudpaymentsRequest request) {
        var entity = mapper.createFromRequest(request);
        transactionService.save(entity);

        //TODO выслать в бот уведомление с кнопкой

        var response = new HashMap<String, Integer>();
        response.put("code", 0);
        return ResponseEntity.ok(response);
    }
}
