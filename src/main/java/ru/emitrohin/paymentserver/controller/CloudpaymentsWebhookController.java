package ru.emitrohin.paymentserver.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.emitrohin.paymentserver.client.BotMotherClient;
import ru.emitrohin.paymentserver.client.TelegramBotClient;
import ru.emitrohin.paymentserver.dto.cloudpayments.CloudpaymentsPaymentStatusCode;
import ru.emitrohin.paymentserver.dto.cloudpayments.CloudpaymentsRequest;
import ru.emitrohin.paymentserver.dto.mapper.TransactionMapper;
import ru.emitrohin.paymentserver.model.SubscriptionStatus;
import ru.emitrohin.paymentserver.service.SubscriptionService;
import ru.emitrohin.paymentserver.service.TransactionService;

@RestController
@RequiredArgsConstructor
//TODO change to api
//TODO client retries and error handling
public class CloudpaymentsWebhookController {

    private final TelegramBotClient telegramBotClient;

    private final BotMotherClient botMotherClient;

    private final TransactionService transactionService;

    private final SubscriptionService subscriptionService;

    private final TransactionMapper mapper;

    //TODO check webhook повторный платеж и проверка accountId
    @PostMapping( "/cloudpayments/success")
    public CloudpaymentsPaymentStatusCode successWebhook(@Valid CloudpaymentsRequest request) {
        var entity = mapper.createFromRequest(request);
        transactionService.save(entity);

        subscriptionService.createOrUpdateCurrentSubscriptionStatus(request.getAccountId(), SubscriptionStatus.PAID);

        //TODO пользователь может запретить писать себе
        telegramBotClient.sendMessageWithButtons("Твоя подписка успешно оплачена! 🎉\n\nВот ссылки для твоего удобства \uD83D\uDC47", request.getAccountId());

        botMotherClient.sendPayload(request.getAccountId());

        return CloudpaymentsPaymentStatusCode.OK;
    }

    @PostMapping( "/cloudpayments/fail")
    public CloudpaymentsPaymentStatusCode failWebhook(@Valid CloudpaymentsRequest request) {
        var entity = mapper.createFromRequest(request);
        transactionService.save(entity);

        //TODO выслать в бот уведомление с кнопкой

        return CloudpaymentsPaymentStatusCode.OK;
    }
}
