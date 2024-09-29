package ru.emitrohin.paymentserver.dto.cloudpayments;

public record FailedPaymentRequest (
        String reason,
        String code
) {
}
