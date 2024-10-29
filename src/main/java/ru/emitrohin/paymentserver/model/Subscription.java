package ru.emitrohin.paymentserver.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "subscriptions")
public class Subscription extends BaseEntity {

    @Column(nullable = false)
    private Long telegramId;

    @Column(nullable = false)
    private LocalDateTime subscriptionStartDate;

    @Column(nullable = false)
    private LocalDateTime subscriptionEndDate;

    @Column(nullable = false)
    private SubscriptionStatus subscriptionStatus;
}
