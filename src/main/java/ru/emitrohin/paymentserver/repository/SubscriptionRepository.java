package ru.emitrohin.paymentserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.emitrohin.paymentserver.model.Subscription;
import ru.emitrohin.paymentserver.model.SubscriptionStatus;

import java.time.LocalDateTime;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findFirstByTelegramIdAndSubscriptionStartDateBetween(Long telegramId, LocalDateTime startDate, LocalDateTime endDate);
    Optional<Subscription> findByTelegramIdAndSubscriptionStatusAndSubscriptionStartDateBetween(
            Long telegramId,
            SubscriptionStatus subscriptionStatus,
            LocalDateTime startOfMonth,
            LocalDateTime endOfMonth);
}