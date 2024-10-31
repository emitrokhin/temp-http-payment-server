package ru.emitrohin.paymentserver.IT;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.emitrohin.paymentserver.client.TelegramBotClient;
import ru.emitrohin.paymentserver.config.MessageConfig;
import ru.emitrohin.paymentserver.model.Subscription;
import ru.emitrohin.paymentserver.model.SubscriptionStatus;
import ru.emitrohin.paymentserver.repository.SubscriptionRepository;
import ru.emitrohin.paymentserver.service.SubscriptionService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private TelegramBotClient telegramBotClient;

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Mock
    private MessageConfig messageConfig;

    private static final Long TELEGRAM_ID = 1234567890L;

    private static final Subscription ACTIVE_SUBSCRIPTION = createTestSubscription(
            TELEGRAM_ID, SubscriptionStatus.PAID, LocalDateTime.now().plusDays(10), LocalDateTime.now().minusDays(20));

    private static final Subscription EXPIRED_SUBSCRIPTION = createTestSubscription(
            TELEGRAM_ID, SubscriptionStatus.EXPIRED, LocalDateTime.now().minusDays(5), LocalDateTime.now().minusMonths(1));

    private static Subscription createTestSubscription(Long telegramId, SubscriptionStatus status, LocalDateTime endDate, LocalDateTime startDate) {
        var subscription = new Subscription();
        subscription.setTelegramId(telegramId);
        subscription.setSubscriptionStatus(status);
        subscription.setSubscriptionEndDate(endDate);
        subscription.setSubscriptionStartDate(startDate);
        return subscription;
    }

    @Test
    public void hasPaidSubscription_ShouldReturnTrue() {
        when(subscriptionRepository.findByTelegramIdAndSubscriptionStatus(TELEGRAM_ID, ACTIVE_SUBSCRIPTION.getSubscriptionStatus()))
                .thenReturn(Optional.of(ACTIVE_SUBSCRIPTION));

        assertTrue(subscriptionService.hasPaidSubscription(TELEGRAM_ID));
        verify(subscriptionRepository).findByTelegramIdAndSubscriptionStatus(eq(TELEGRAM_ID), eq(ACTIVE_SUBSCRIPTION.getSubscriptionStatus()));
    }

    @Test
    public void hasPaidSubscription_ShouldReturnFalse() {
        when(subscriptionRepository.findByTelegramIdAndSubscriptionStatus(TELEGRAM_ID, SubscriptionStatus.PAID))
                .thenReturn(Optional.empty());
        assertFalse(subscriptionService.hasPaidSubscription(TELEGRAM_ID));
        verify(subscriptionRepository).findByTelegramIdAndSubscriptionStatus(eq(TELEGRAM_ID), eq(SubscriptionStatus.PAID));
    }

    @Test
    public void extendSubscription_ShouldExtendSubscriptionEndDateByOneMonth() {
        var initialEndDate = ACTIVE_SUBSCRIPTION.getSubscriptionEndDate();
        var subscription = ACTIVE_SUBSCRIPTION;


        subscriptionService.extendSubscription(TELEGRAM_ID);
        subscription.setSubscriptionEndDate(initialEndDate.plusMonths(1));
        subscriptionRepository.save(subscription);

        assertEquals(initialEndDate.plusMonths(1), subscription.getSubscriptionEndDate());

        verify(subscriptionRepository).save(subscription);
    }

    @Test
    public void shouldExpireSubscriptionWhenEndDateHasPassed() {
        when(subscriptionRepository.findFirstBySubscriptionEndDateBeforeAndSubscriptionStatus(any(LocalDateTime.class), eq(SubscriptionStatus.PAID)))
                .thenReturn(List.of(EXPIRED_SUBSCRIPTION));

        when(subscriptionRepository.findFirstByTelegramId(EXPIRED_SUBSCRIPTION.getTelegramId()))
                .thenReturn(Optional.of(EXPIRED_SUBSCRIPTION));

        subscriptionService.checkSubscriptionsAndNotifyExpired();

        verify(subscriptionRepository).findFirstBySubscriptionEndDateBeforeAndSubscriptionStatus(any(LocalDateTime.class), eq(SubscriptionStatus.PAID));

        verify(telegramBotClient).removeFromTelegramGroup(EXPIRED_SUBSCRIPTION.getTelegramId());
        verify(telegramBotClient).sendExpirationNotification(EXPIRED_SUBSCRIPTION.getTelegramId());

        verify(subscriptionRepository).save(EXPIRED_SUBSCRIPTION);
    }

    @Test
    public void shouldSendReminderWhenSubscriptionIsAboutThreeDaysToExpire() {
        var threeDaysBeforeExpire = createTestSubscription(
                TELEGRAM_ID, SubscriptionStatus.PAID, LocalDateTime.now().plusDays(3), LocalDateTime.now().minusDays(1));

        when(subscriptionRepository.findFirstBySubscriptionEndDateAfterAndSubscriptionStatus(any(LocalDateTime.class), eq(SubscriptionStatus.PAID)))
                .thenReturn(List.of(threeDaysBeforeExpire));

        when(messageConfig.getSubscriptionRenewalFirstReminder()).thenReturn("До конца вашей подписки осталось 3 дня");

        subscriptionService.checkSubscriptionsAndNotifyExpired();

        verify(subscriptionRepository).findFirstBySubscriptionEndDateAfterAndSubscriptionStatus(any(LocalDateTime.class), eq(SubscriptionStatus.PAID));
        verify(telegramBotClient).sendMessage(eq(threeDaysBeforeExpire.getTelegramId()), contains("До конца вашей подписки осталось 3 дня"));
    }

    @Test
    public void shouldSendReminderWhenSubscriptionIsAboutOneDayToExpire() {
        var oneDayBeforeExpire = createTestSubscription(
                TELEGRAM_ID, SubscriptionStatus.PAID, LocalDateTime.now().plusDays(1), LocalDateTime.now().minusDays(1));

        when(subscriptionRepository.findFirstBySubscriptionEndDateAfterAndSubscriptionStatus(any(LocalDateTime.class), eq(SubscriptionStatus.PAID)))
                .thenReturn(List.of(oneDayBeforeExpire));

        when(messageConfig.getSubscriptionRenewalSecondReminder()).thenReturn("До конца вашей подписки остался 1 день");

        subscriptionService.checkSubscriptionsAndNotifyExpired();

        verify(subscriptionRepository).findFirstBySubscriptionEndDateAfterAndSubscriptionStatus(any(LocalDateTime.class), eq(SubscriptionStatus.PAID));
        verify(telegramBotClient).sendMessage(eq(oneDayBeforeExpire.getTelegramId()), contains("До конца вашей подписки остался 1 день"));
    }

    @Test
    public void shouldRemoveUserFromGroupWhenSubscriptionExpires() {
        when(subscriptionRepository.findFirstBySubscriptionEndDateBeforeAndSubscriptionStatus(any(LocalDateTime.class), eq(SubscriptionStatus.PAID)))
                .thenReturn(List.of(EXPIRED_SUBSCRIPTION));

        when(subscriptionRepository.findFirstByTelegramId(EXPIRED_SUBSCRIPTION.getTelegramId()))
                .thenReturn(Optional.of(EXPIRED_SUBSCRIPTION));

        subscriptionService.checkSubscriptionsAndNotifyExpired();

        verify(telegramBotClient).removeFromTelegramGroup(EXPIRED_SUBSCRIPTION.getTelegramId());
        verify(telegramBotClient).sendExpirationNotification(EXPIRED_SUBSCRIPTION.getTelegramId());

        verify(subscriptionRepository).save(EXPIRED_SUBSCRIPTION);
    }

}
