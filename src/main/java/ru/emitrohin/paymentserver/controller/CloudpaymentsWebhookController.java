package ru.emitrohin.paymentserver.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.emitrohin.paymentserver.dto.CloudpaymentsPaymentStatusCode;
import ru.emitrohin.paymentserver.dto.CloudpaymentsRequest;
import ru.emitrohin.paymentserver.dto.mapper.TransactionMapper;
import ru.emitrohin.paymentserver.model.SubscriptionStatus;
import ru.emitrohin.paymentserver.service.SubscriptionService;
import ru.emitrohin.paymentserver.service.TransactionService;

@RestController
@RequiredArgsConstructor
public class CloudpaymentsWebhookController {

    private final TransactionService transactionService;

    private final SubscriptionService subscriptionService;

    private final TransactionMapper mapper;

    //TODO check webhook повторный платеж и проверка accountId

    @PostMapping( "/cloudpayments/success")
    public CloudpaymentsPaymentStatusCode successWebhook(@Valid CloudpaymentsRequest request) {
        var entity = mapper.createFromRequest(request);
        transactionService.save(entity);

        subscriptionService.createOrUpdateCurrentSubscriptionStatus(request.getAccountId(), SubscriptionStatus.PAID);

        //TODO выслать в бот уведомление с кнопкой

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
