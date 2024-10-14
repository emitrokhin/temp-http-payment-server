package ru.emitrohin.paymentserver.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionDTO {
    private String firstName;
    private String lastName;
    private BigDecimal amount;
    private LocalDateTime dateTime;
    private String currency;
}

