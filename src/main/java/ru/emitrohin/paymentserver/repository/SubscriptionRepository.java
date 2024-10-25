package ru.emitrohin.paymentserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.emitrohin.paymentserver.model.Subscription;
import ru.emitrohin.paymentserver.model.SubscriptionStatus;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findFirstByTelegramIdAndSubscriptionStartDateBetween(Long telegramId, LocalDateTime startDate, LocalDateTime endDate);
    Optional<Subscription> findByTelegramIdAndSubscriptionStatusAndSubscriptionStartDateBetween(
            Long telegramId,
            SubscriptionStatus subscriptionStatus,
            LocalDateTime startOfMonth,
            LocalDateTime endOfMonth);
    Optional<Subscription> findByTelegramIdAndSubscriptionStatus(Long telegramId, SubscriptionStatus subscriptionStatus);
    Optional<Subscription> findFirstByTelegramIdAndSubscriptionEndDateAfter(Long telegramId, LocalDateTime endDate);
    List<Subscription> findAllBySubscriptionEndDateBeforeAndSubscriptionStatus(LocalDateTime endDate, SubscriptionStatus subscriptionStatus);
    Optional<Subscription> findFirstByTelegramIdAndSubscriptionEndDateBefore(Long telegramId, LocalDateTime endDate);
    Optional<Subscription> findFirstByTelegramId(Long telegramId);
    List<Subscription> findAllBySubscriptionEndDateAfterAndSubscriptionStatus(LocalDateTime endDate, SubscriptionStatus subscriptionStatus);
}