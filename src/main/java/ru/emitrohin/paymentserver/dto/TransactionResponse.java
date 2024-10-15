package ru.emitrohin.paymentserver.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        BigDecimal amount,
        LocalDateTime dateTime,
        String currency) {}

