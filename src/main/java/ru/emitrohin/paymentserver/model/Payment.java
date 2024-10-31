package ru.emitrohin.paymentserver.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment extends BaseEntity {

    @Column(nullable = false)
    private long telegramId;

    @Column(nullable = false)
    private PaymentStatus paymentStatus;

}
