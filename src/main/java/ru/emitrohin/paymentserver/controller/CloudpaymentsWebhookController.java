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
import ru.emitrohin.paymentserver.dto.mapper.CardMapper;
import ru.emitrohin.paymentserver.dto.mapper.TransactionMapper;
import ru.emitrohin.paymentserver.model.Card;
import ru.emitrohin.paymentserver.model.SubscriptionStatus;
import ru.emitrohin.paymentserver.service.CardService;
import ru.emitrohin.paymentserver.service.SubscriptionService;
import ru.emitrohin.paymentserver.service.TransactionService;

import java.time.LocalDate;
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

    private final TransactionMapper transactionMapper;

    private final CardService cardService;

    private final CardMapper cardMapper;

    //TODO check webhook повторный платеж и проверка accountId
    @PostMapping("/cloudpayments/success")
    public ResponseEntity<Map<String, Integer>> successWebhook(@Valid CloudpaymentsRequest request) {
        try {
            var telegramId = Long.parseLong(request.getAccountId());
            var transaction = transactionMapper.createFromRequest(request);

            // Сохранение транзакции
            transactionService.save(transaction);

            var existingCard = cardService.getCardByCardId(request.getCardId());
            if (existingCard == null) {
                cardService.deactivatePrimaryForAllCards(telegramId);
                var card = cardMapper.createFromRequest(request);
                card.setTelegramId(telegramId);
                card.setIsPrimary(true);
                card.setIsActive(true);
                cardService.saveCard(card);
            }
            // Обновление подписки
            subscriptionService.createOrUpdateCurrentSubscriptionStatus(telegramId, SubscriptionStatus.PAID);

            // Отправка сообщения через Telegram
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
        var transaction = transactionMapper.createFromRequest(request);
        transactionService.save(transaction);

        // Проверка, существует ли карта с указанным cardId
        var card = cardService.getCardByCardId(request.getCardId());

        if (card != null) {
            // Деактивация карты в случае неудачной транзакции
            cardService.deactivateCard(card.getCardId());
        }

        //TODO выслать в бот уведомление с кнопкой

        var response = new HashMap<String, Integer>();
        response.put("code", 0);
        return ResponseEntity.ok(response);
    }
}
