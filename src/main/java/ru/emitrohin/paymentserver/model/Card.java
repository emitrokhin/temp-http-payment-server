package ru.emitrohin.paymentserver.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cards")
public class Card extends BaseEntity {

    @Column(nullable = false)
    private long telegramId;

    @Column(nullable = false)
    private String cardId;             // Уникальный идентификатор карты в системе CloudPayments

    @Column(nullable = false)
    private int cardLastFour;       // Последние четыре цифры номера карты

    @Column(nullable = false)
    private String cardType;           // Тип карты (Visa; MasterCard и т.д.)

    @Column(nullable = false)
    private String cardExpDate;        // Срок действия карты в формате MM/YY

    private String token;              // Токен карты для повторных платежей без ввода реквизитов

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private Boolean isPrimary;
}