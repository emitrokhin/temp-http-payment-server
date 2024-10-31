package ru.emitrohin.paymentserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.emitrohin.paymentserver.client.TelegramBotClient;
import ru.emitrohin.paymentserver.config.MessageConfig;
import ru.emitrohin.paymentserver.model.Subscription;
import ru.emitrohin.paymentserver.model.SubscriptionStatus;
import ru.emitrohin.paymentserver.repository.SubscriptionRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@EnableScheduling
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final TelegramBotClient telegramBotClient;
    private final MessageConfig messageConfig;

    public Optional<Subscription> findCurrentSubscription(long telegramId) {
        return subscriptionRepository.findFirstByTelegramIdAndSubscriptionEndDateAfter(
                telegramId, LocalDateTime.now());
    }

    public void save(Subscription subscription) {
        subscriptionRepository.save(subscription);
    }

    public boolean hasPaidSubscription(long telegramId) {
        return subscriptionRepository.findByTelegramIdAndSubscriptionStatus(telegramId, SubscriptionStatus.PAID)
                .filter(subscription -> subscription.getSubscriptionEndDate().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    private Subscription createSubscription(long telegramId, SubscriptionStatus status, int monthsDuration) {
        var subscription = new Subscription();
        subscription.setTelegramId(telegramId);
        subscription.setSubscriptionStatus(status);
        subscription.setSubscriptionStartDate(LocalDateTime.now());
        subscription.setSubscriptionEndDate(LocalDateTime.now().plusMonths(monthsDuration));
        return subscription;
    }

    public void createPendingSubscription(long telegramId) {
        save(createSubscription(telegramId, SubscriptionStatus.PENDING, 1));
    }

    public void createPaidSubscription(long telegramId) {
        save(createSubscription(telegramId, SubscriptionStatus.PAID, 1));
    }

    //TODO рефакторинг + hasPaidSubscription + findCurrent
    public void createOrUpdateCurrentSubscriptionStatus(long telegramId, SubscriptionStatus status) {
        findCurrentSubscription(telegramId).ifPresentOrElse(
                subscription -> {
                    subscription.setSubscriptionStatus(status);
                    save(subscription);
                },
                () -> createPaidSubscription(telegramId)
        );
    }

    public void extendSubscription(long telegramId) {
        subscriptionRepository.findFirstByTelegramIdAndSubscriptionStatus(telegramId, SubscriptionStatus.PAID)
                .ifPresent(subscription -> {
                    // Обновляем статус текущей подписки на EXTENDED
                    subscription.setSubscriptionStatus(SubscriptionStatus.EXTENDED);
                    save(subscription);

                    // Создаем новую запись подписки со статусом PAID и новым сроком действия
                    var newSubscription = new Subscription();
                    newSubscription.setTelegramId(telegramId);
                    newSubscription.setSubscriptionStartDate(subscription.getSubscriptionEndDate());
                    newSubscription.setSubscriptionEndDate(subscription.getSubscriptionEndDate().plusMonths(1));
                    newSubscription.setSubscriptionStatus(SubscriptionStatus.PAID);
                    save(newSubscription);
                });
    }

    @Scheduled(cron = "0 0 0 * * *") // Каждый день в полночь
    public void expireSubscriptionsDaily() {
        var now = LocalDateTime.now();

        subscriptionRepository.findFirstBySubscriptionEndDateBeforeAndSubscriptionStatus(now, SubscriptionStatus.PAID)
                .forEach(subscription -> expireSubscription(subscription.getTelegramId()));
    }

    @Scheduled(cron = "0 5 0 * * *") // Каждый день в 00:05
    public void sendExpiryRemindersDaily() {
        var threeDaysBeforeEnd = LocalDateTime.now().plusDays(3);
        var oneDayBeforeEnd = LocalDateTime.now().plusDays(1);

        subscriptionRepository.findFirstBySubscriptionEndDateAfterAndSubscriptionStatus(threeDaysBeforeEnd, SubscriptionStatus.PAID)
                .forEach(subscription -> sendReminderIfNeeded(subscription, threeDaysBeforeEnd, oneDayBeforeEnd));
    }

    private void sendReminderIfNeeded(Subscription subscription, LocalDateTime threeDaysBeforeEnd, LocalDateTime oneDayBeforeEnd) {
        var telegramId = subscription.getTelegramId();
        var endDate = subscription.getSubscriptionEndDate();

        if (endDate.isBefore(threeDaysBeforeEnd)) {
            telegramBotClient.sendMessage(telegramId, messageConfig.getSubscriptionRenewalFirstReminder());
        } else if (endDate.isBefore(oneDayBeforeEnd)) {
            telegramBotClient.sendMessage(telegramId, messageConfig.getSubscriptionRenewalSecondReminder());
        }
    }

    private void expireSubscription(long telegramId) {
        subscriptionRepository.findFirstByTelegramId(telegramId)
                .ifPresent(subscription -> {
                    subscription.setSubscriptionStatus(SubscriptionStatus.EXPIRED);
                    save(subscription);
                    telegramBotClient.removeFromTelegramGroup(telegramId);
                    telegramBotClient.sendExpirationNotification(telegramId);
                });
    }

}
