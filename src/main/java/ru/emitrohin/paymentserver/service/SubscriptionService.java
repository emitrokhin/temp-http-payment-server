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
        subscriptionRepository.findFirstByTelegramIdAndSubscriptionEndDateAfter(telegramId, LocalDateTime.now())
                .ifPresent(
                        subscription -> {
                            // Продлеваем подписку на еще один месяц с текущей даты окончания
                            subscription.setSubscriptionEndDate(subscription.getSubscriptionEndDate().plusMonths(1));
                            save(subscription);
                        }
                );
    }

    @Scheduled(cron = "0 0 0 * * *") // Каждый день в полночь
    public void checkSubscriptionsAndNotifyExpired() {
        var now = LocalDateTime.now();

        subscriptionRepository.findAllBySubscriptionEndDateBeforeAndSubscriptionStatus(now, SubscriptionStatus.PAID)
                .forEach(subscription -> expireSubscription(subscription.getTelegramId()));

        var threeDaysBeforeEnd = now.plusDays(3);

        subscriptionRepository.findAllBySubscriptionEndDateAfterAndSubscriptionStatus(threeDaysBeforeEnd, SubscriptionStatus.PAID)
                .forEach(subscription -> {
                    if (subscription.getSubscriptionEndDate().isBefore(threeDaysBeforeEnd)) {
                        telegramBotClient.sendMessage(subscription.getTelegramId(), messageConfig.getSubscriptionReminder());
                    }
                });
    }

    public void expireSubscription(long telegramId) {
        subscriptionRepository.findFirstByTelegramId(telegramId)
                .ifPresent(subscription -> {
                    subscription.setSubscriptionStatus(SubscriptionStatus.EXPIRED);
                    save(subscription);
                    telegramBotClient.removeFromTelegramGroup(telegramId);
                    telegramBotClient.sendExpirationNotification(telegramId);
                    telegramBotClient.verifyUserLeftGroup(telegramId);
                });
    }

}
