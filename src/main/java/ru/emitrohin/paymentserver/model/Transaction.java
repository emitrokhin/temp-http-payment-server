package ru.emitrohin.paymentserver.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private long telegramId;         // Идентификатор пользователя или аккаунта

    @Column(nullable = false)
    private long transactionId;        // Идентификатор транзакции

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;             // Сумма платежа

    @Column(nullable = false)
    private String currency;           // Валюта платежа

    @Column(nullable = false)
    private BigDecimal paymentAmount;      // Сумма списания

    @Column(nullable = false)
    private String paymentCurrency;    // Сумма списания

    @Column(nullable = false)
    private LocalDateTime dateTime;    // Дата и время проведения операции

    @Column
    private String cardId;             // Уникальный идентификатор карты в системе CloudPayments

    @Column(nullable = false)
    private int cardFirstSix;       // Первые шесть цифр номера карты

    @Column(nullable = false)
    private int cardLastFour;       // Последние четыре цифры номера карты

    @Column(nullable = false)
    private String cardType;           // Тип карты (Visa; MasterCard и т.д.)

    @Column(nullable = false)
    private String cardExpDate;        // Срок действия карты в формате MM/YY

    @Column(nullable = false)
    private byte testMode;          // Признак тестового режима

    @Column
    private String reason;             // Причина отказа

    @Column
    private int reasonCode;         // Код ошибки

    @Column(nullable = false)
    private String status;             // Статус транзакции

    @Column(nullable = false)
    private String operationType;      // Тип операции: Payment/CardPayout

    @Column(nullable = false)
    private String invoiceId;          // Номер заказа из параметров платежа

    @Column
    private String subscriptionId;     // Идентификатор подписки (для рекуррентных платежей)

    @Column
    private String email;              // Email плательщика

    @Column
    private String token;              // Токен карты для повторных платежей без ввода реквизитов

    @Column
    private String rrn;                // Уникальный номер банковской транзакции; который назначается обслуживающим банком
}