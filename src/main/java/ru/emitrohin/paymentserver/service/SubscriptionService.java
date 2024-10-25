package ru.emitrohin.paymentserver.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.emitrohin.paymentserver.client.TelegramBotClient;
import ru.emitrohin.paymentserver.model.Subscription;
import ru.emitrohin.paymentserver.model.SubscriptionStatus;
import ru.emitrohin.paymentserver.repository.SubscriptionRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@EnableScheduling
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final TelegramBotClient telegramBotClient;

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

    public void createPendingSubscription(long telegramId) {
        var subscription = new Subscription();
        subscription.setTelegramId(telegramId);
        subscription.setSubscriptionStatus(SubscriptionStatus.PENDING);
        subscription.setSubscriptionStartDate(LocalDateTime.now());
        subscription.setSubscriptionEndDate(LocalDateTime.now().plusMonths(1));
        subscriptionRepository.save(subscription);
    }

    public void createPaidSubscription(long telegramId) {
        var subscription = new Subscription();
        subscription.setTelegramId(telegramId);
        subscription.setSubscriptionStatus(SubscriptionStatus.PAID);
        subscription.setSubscriptionStartDate(LocalDateTime.now());
        subscriptionRepository.save(subscription);
    }

    //TODO рефакторинг + hasPaidSubscription + findCurrent
    public void createOrUpdateCurrentSubscriptionStatus(long telegramId, SubscriptionStatus status) {
        subscriptionRepository.findFirstByTelegramIdAndSubscriptionEndDateAfter(telegramId, LocalDateTime.now())
                .ifPresentOrElse(
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

    //    @Scheduled(cron = "0 0 0 * * *") // Каждый день в полночь
    @Scheduled(cron = "*/10 * * * * *")
    public void checkExpiredSubscriptions() {
        var now = LocalDateTime.now();

        subscriptionRepository.findAllBySubscriptionEndDateBeforeAndSubscriptionStatus(now, SubscriptionStatus.PAID)
                .forEach(subscription -> expireSubscription(subscription.getTelegramId()));

        // Проверяем, если осталось 3 дня до конца подписки
        var threeDaysBeforeEnd = now.plusDays(3);

        subscriptionRepository.findAllBySubscriptionEndDateAfterAndSubscriptionStatus(threeDaysBeforeEnd, SubscriptionStatus.PAID)
                .forEach(subscription -> {
                    if (subscription.getSubscriptionEndDate().isBefore(threeDaysBeforeEnd)) {
                        // Отправляем напоминание
                        telegramBotClient.sendMessage(subscription.getTelegramId(), "До конца вашей подписки осталось 3 дня. Пожалуйста, продлите подписку.");
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
