package ru.emitrohin.paymentserver.dto;

public record CardResponse(
        int cardLastFour,
        String cardExpDate,
        Boolean isActive,
        Boolean isPrimary,
        String cardType,
        String cardId,
        String token) {
}
