package ru.emitrohin.paymentserver.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private Long telegramId;

    @Column(nullable = false)
    private LocalDateTime subscriptionDate;

    @Column(nullable = false)
    private SubscriptionStatus subscriptionStatus;
}