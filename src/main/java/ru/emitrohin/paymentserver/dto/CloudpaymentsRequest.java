package ru.emitrohin.paymentserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//Не работает с record
@Data
@AllArgsConstructor
public class CloudpaymentsRequest {

        private Long TransactionId;        // Идентификатор транзакции

        private BigDecimal Amount;         // Сумма платежа

        private String Currency;           // Валюта платежа

        private BigDecimal PaymentAmount;  // Сумма списания

        private String PaymentCurrency;    // Валюта списания

        private String OperationType;      // Тип операции

        private String InvoiceId;          // Номер заказа

        private Long AccountId;            // Идентификатор пользователя

        private String SubscriptionId;     // Идентификатор подписки

        private String Email;              // Email плательщика

        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime DateTime;    // Дата и время операции

        private String Reason;             // Причина отказа

        private Integer ReasonCode;        // Код причины отказа

        private String CardId;             // Уникальный идентификатор карты

        private Integer CardFirstSix;       // Первые 6 цифр номера карты

        private Integer CardLastFour;       // Последние 4 цифры номера карты

        private String CardType;           // Тип карты (Visa, MasterCard и т.д.)

        private String CardExpDate;        // Срок действия карты в формате MM/YY

        private String Issuer;             // Название банка-эмитента карты

        private String Token;              // Токен карты для повторных платежей

        private Byte TestMode;           // Признак тестового режима

        private String Status;             // Статус транзакции

        private String PaymentMethod;      // Метод оплаты (ApplePay, GooglePay и т.д.)
}
