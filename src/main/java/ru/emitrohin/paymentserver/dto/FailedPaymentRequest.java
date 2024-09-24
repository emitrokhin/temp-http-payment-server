package ru.emitrohin.paymentserver.dto;

public record FailedPaymentRequest (
        String reason,
        String code
) {
}
