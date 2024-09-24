package ru.emitrohin.paymentserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.emitrohin.paymentserver.model.Subscription;
import ru.emitrohin.paymentserver.model.SubscriptionStatus;
import ru.emitrohin.paymentserver.repository.SubscriptionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public Optional<Subscription> findCurrentSubscription(long telegramId) {
        // Начало текущего месяца
        var startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        // Начало следующего месяца
        var startOfNextMonth = startOfMonth.plusMonths(1);

        // Поиск подписки в репозитории
        return subscriptionRepository.findFirstByTelegramIdAndSubscriptionDateBetween(
                telegramId, startOfMonth, startOfNextMonth);
    }

    public void save(Subscription subscription) {
        subscriptionRepository.save(subscription);
    }

    public boolean hasPaidSubscription(long telegramId) {
        // Начало текущего месяца
        var startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        // Начало следующего месяца
        var startOfNextMonth = startOfMonth.plusMonths(1);

        return subscriptionRepository.findByTelegramIdAndSubscriptionStatusAndSubscriptionDateBetween(
                telegramId, SubscriptionStatus.PAID, startOfMonth, startOfNextMonth).isPresent();
    }

    public void createPendingSubscription(long telegramId) {
        var subscription = new Subscription();
        subscription.setTelegramId(telegramId);
        subscription.setSubscriptionStatus(SubscriptionStatus.PENDING);
        subscription.setSubscriptionDate(LocalDateTime.now());
        subscriptionRepository.save(subscription);
    }

    public void createPaidSubscription(long telegramId) {
        var subscription = new Subscription();
        subscription.setTelegramId(telegramId);
        subscription.setSubscriptionStatus(SubscriptionStatus.PAID);
        subscription.setSubscriptionDate(LocalDateTime.now());
        subscriptionRepository.save(subscription);
    }

    //TODO рефакторинг + hasPaidSubscription + findCurrent
    public void createOrUpdateCurrentSubscriptionStatus(long telegramId, SubscriptionStatus status) {
        // Начало текущего месяца
        var startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        // Начало следующего месяца
        var startOfNextMonth = startOfMonth.plusMonths(1);

        subscriptionRepository.findFirstByTelegramIdAndSubscriptionDateBetween(telegramId, startOfMonth, startOfNextMonth)
                .ifPresentOrElse(
                        subscription -> {
                            subscription.setSubscriptionStatus(status);
                            save(subscription);
                        },
                        () -> createPaidSubscription(telegramId)
                );
    }
}
