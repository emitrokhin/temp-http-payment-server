package ru.emitrohin.paymentserver.dto;

import lombok.Getter;

@Getter
public enum CloudpaymentsPaymentStatusCode {
    OK(0),                       // Платеж может быть проведен
    INVALID_ORDER_NUMBER(10),    // Неверный номер заказа
    INVALID_ACCOUNT_ID(11),      // Некорректный AccountId
    INVALID_AMOUNT(12),          // Неверная сумма
    PAYMENT_REJECTED(13),        // Платеж не может быть принят
    PAYMENT_EXPIRED(20);         // Платеж просрочен

    private final int code;

    CloudpaymentsPaymentStatusCode(int code) {
        this.code = code;
    }

}
