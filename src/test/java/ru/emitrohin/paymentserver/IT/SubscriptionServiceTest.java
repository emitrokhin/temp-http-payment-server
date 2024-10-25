package ru.emitrohin.paymentserver.IT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.emitrohin.paymentserver.client.BotMotherClient;
import ru.emitrohin.paymentserver.client.TelegramBotClient;
import ru.emitrohin.paymentserver.controller.CloudpaymentsWebhookController;
import ru.emitrohin.paymentserver.controller.MainController;
import ru.emitrohin.paymentserver.controller.PaymentController;
import ru.emitrohin.paymentserver.controller.ProfileController;
import ru.emitrohin.paymentserver.controller.rest.CardController;
import ru.emitrohin.paymentserver.controller.rest.FirstRunRestController;
import ru.emitrohin.paymentserver.model.Subscription;
import ru.emitrohin.paymentserver.model.SubscriptionStatus;
import ru.emitrohin.paymentserver.repository.SubscriptionRepository;
import ru.emitrohin.paymentserver.security.TelegramPreAuthenticatedProcessingFilter;
import ru.emitrohin.paymentserver.service.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@WebMvcTest(SubscriptionService.class)
public class SubscriptionServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubscriptionRepository subscriptionRepository;

    @MockBean
    private TelegramBotClient telegramBotClient;

    @Autowired
    private SubscriptionService subscriptionService;

    @MockBean
    private TelegramPreAuthenticatedProcessingFilter telegramPreAuthenticatedProcessingFilter;

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

        when(subscriptionRepository.findFirstByTelegramIdAndSubscriptionEndDateAfter(TELEGRAM_ID, subscription.getSubscriptionEndDate()))
                .thenReturn(Optional.of(subscription));

        subscriptionService.extendSubscription(TELEGRAM_ID);
        subscription.setSubscriptionEndDate(initialEndDate.plusMonths(1));
        subscriptionRepository.save(subscription);

        assertEquals(initialEndDate.plusMonths(1), subscription.getSubscriptionEndDate());

        verify(subscriptionRepository).save(subscription);
    }

    @Test
    public void checkExpiredSubscriptions_ShouldExpireAndSendReminder() {
        when(subscriptionRepository.findAllBySubscriptionEndDateBeforeAndSubscriptionStatus(any(LocalDateTime.class), eq(SubscriptionStatus.PAID)))
                .thenReturn(List.of(EXPIRED_SUBSCRIPTION));

        var soonToExpireSubscription = createTestSubscription(
                TELEGRAM_ID, SubscriptionStatus.PAID, LocalDateTime.now().plusDays(3), LocalDateTime.now().minusDays(20));
        when(subscriptionRepository.findAllBySubscriptionEndDateAfterAndSubscriptionStatus(any(LocalDateTime.class), eq(SubscriptionStatus.PAID)))
                .thenReturn(List.of(soonToExpireSubscription));

        when(subscriptionRepository.findFirstByTelegramId(EXPIRED_SUBSCRIPTION.getTelegramId()))
                .thenReturn(Optional.of(EXPIRED_SUBSCRIPTION));

        subscriptionService.checkExpiredSubscriptions();

        verify(subscriptionRepository).findAllBySubscriptionEndDateBeforeAndSubscriptionStatus(any(LocalDateTime.class), eq(SubscriptionStatus.PAID));
        verify(telegramBotClient).removeFromTelegramGroup(EXPIRED_SUBSCRIPTION.getTelegramId());
        verify(telegramBotClient).sendExpirationNotification(EXPIRED_SUBSCRIPTION.getTelegramId());
        verify(telegramBotClient).verifyUserLeftGroup(EXPIRED_SUBSCRIPTION.getTelegramId());
        verify(subscriptionRepository).save(EXPIRED_SUBSCRIPTION);

        verify(subscriptionRepository).findAllBySubscriptionEndDateAfterAndSubscriptionStatus(any(LocalDateTime.class), eq(SubscriptionStatus.PAID));
        verify(telegramBotClient).sendMessage(eq(soonToExpireSubscription.getTelegramId()), contains("До конца вашей подписки осталось 3 дня"));
    }

}
