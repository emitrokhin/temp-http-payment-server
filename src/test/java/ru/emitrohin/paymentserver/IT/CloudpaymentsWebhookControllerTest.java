package ru.emitrohin.paymentserver.IT;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.emitrohin.paymentserver.client.BotMotherClient;
import ru.emitrohin.paymentserver.client.TelegramBotClient;
import ru.emitrohin.paymentserver.controller.CloudpaymentsWebhookController;
import ru.emitrohin.paymentserver.controller.PaymentController;
import ru.emitrohin.paymentserver.controller.ProfileController;
import ru.emitrohin.paymentserver.dto.cloudpayments.CloudpaymentsRequest;
import ru.emitrohin.paymentserver.dto.mapper.CardMapper;
import ru.emitrohin.paymentserver.dto.mapper.ProfileMapper;
import ru.emitrohin.paymentserver.dto.mapper.TransactionMapper;
import ru.emitrohin.paymentserver.dto.profile.ProfilePaymentDTO;
import ru.emitrohin.paymentserver.model.*;
import ru.emitrohin.paymentserver.repository.CardRepository;
import ru.emitrohin.paymentserver.repository.SubscriptionRepository;
import ru.emitrohin.paymentserver.repository.TransactionRepository;
import ru.emitrohin.paymentserver.security.TelegramPreAuthenticatedProcessingFilter;
import ru.emitrohin.paymentserver.service.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;



@WebMvcTest(CloudpaymentsWebhookController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CloudpaymentsWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private FirstRunService firstRunService;

    @MockBean
    private ProfileMapper profileMapper;

    @MockBean
    private PaymentController paymentController;

    @MockBean
    private ProfileController profileController;

    @MockBean
    private TelegramPreAuthenticatedProcessingFilter telegramPreAuthenticatedProcessingFilter;

    @MockBean
    private TelegramBotClient telegramBotClient;

    @MockBean
    private BotMotherClient botMotherClient;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private SubscriptionService subscriptionService;

    @MockBean
    private TransactionMapper transactionMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CardService cardService;

    @MockBean
    private TransactionRepository transactionRepository;

    @MockBean
    private CardRepository cardRepository;

    @MockBean
    private SubscriptionRepository subscriptionRepository;

    @MockBean
    private CardMapper cardMapper;

    @MockBean
    private PaymentService paymentService;


    private static final Long TELEGRAM_ID = 1234567890L;
    private static final Transaction TEST_TRANSACTION1 = createTestTransaction(BigDecimal.valueOf(100),
            LocalDateTime.now(), "RUB");
    private static final Transaction TEST_TRANSACTION2 = createTestTransaction(BigDecimal.valueOf(200),
            LocalDateTime.now().minusDays(1), "RUB");
    private static final Card TEST_CARD1 = createTestCard(3055, "05/55", true,
            true, "VISA", "12345");
    private static final Card TEST_CARD2 = createTestCard(2222, "03/33", true,
            false, "MASTERCARD", "54321");
    private static final UUID PAYMENT_ID = UUID.randomUUID();
    private static final Payment TEST_PAYMENT = createTestPayment(PAYMENT_ID, TELEGRAM_ID, PaymentStatus.PENDING);

    private static Payment createTestPayment(UUID id, long telegramId, PaymentStatus paymentStatus) {
        var payment = new Payment();
        payment.setId(id);
        payment.setPaymentStatus(paymentStatus);
        payment.setTelegramId(telegramId);
        return payment;
    }

    private static Transaction createTestTransaction(BigDecimal amount, LocalDateTime dateTime, String currency) {
        var transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDateTime(dateTime);
        transaction.setCurrency(currency);
        return transaction;
    }

    private static Card createTestCard(int cardLastFour, String cardExpDate, Boolean isActive, Boolean isPrimary,
                                       String cardType, String cardId) {
        var card = new Card();
        card.setCardLastFour(cardLastFour);
        card.setCardExpDate(cardExpDate);
        card.setIsActive(isActive);
        card.setIsPrimary(isPrimary);
        card.setCardType(cardType);
        card.setCardId(cardId);
        return card;
    }

    private CloudpaymentsRequest createTestRequest() {
        return new CloudpaymentsRequest(
                123L,                          // TransactionId
                TEST_TRANSACTION1.getAmount(), // Amount из Transaction
                TEST_TRANSACTION1.getCurrency(), // Currency из Transaction
                TEST_TRANSACTION1.getAmount(), // PaymentAmount из Transaction
                TEST_TRANSACTION1.getCurrency(), // PaymentCurrency из Transaction
                "Payment",                     // OperationType
                "INV123",                      // InvoiceId
                String.valueOf(TELEGRAM_ID),       // AccountId (ID пользователя) из Card
                "SUBS123",                     // SubscriptionId (ID подписки)
                "test@test.com",               // Email
                LocalDateTime.now(),           // DateTime (текущая дата/время)
                "reason",                          // Reason (null, так как платеж успешен)
                1,                          // ReasonCode (null, т.к. платеж успешен)
                TEST_CARD1.getCardId(),       // CardId (уникальный идентификатор карты) из Card
                123456,                        // CardFirstSix (первые 6 цифр карты)
                TEST_CARD1.getCardLastFour(), // CardLastFour (последние 4 цифры карты) из Card
                TEST_CARD1.getCardType(),     // CardType (тип карты) из Card
                TEST_CARD1.getCardExpDate(),  // CardExpDate (срок действия карты) из Card
                "BankName",                    // Issuer (имя банка-эмитента)
                "token123",                    // Token (токен для повторных платежей)
                (byte) 0,                      // TestMode (0 - не тестовый режим)
                "Success",                     // Status (статус транзакции)
                "ApplePay"                     // PaymentMethod (метод оплаты)
        );
    }


    @Test
    @WithMockUser(username = "1234567890")
    void successWebhook_ShouldSaveTransactionAndCardAndSendTelegramMessage() throws Exception {// Мокируем SecurityContext
        var transactionRequest = createTestRequest();

        // Настройка мока для сервисов
        when(cardService.getCardByCardId(transactionRequest.getCardId())).thenReturn(null);
        when(paymentService.getLastPendingPayment(TELEGRAM_ID)).thenReturn(Optional.of(TEST_PAYMENT));

        paymentService.updatePaymentStatus(PAYMENT_ID, PaymentStatus.SUCCESS);
        cardService.saveCard(TEST_CARD1);
        transactionService.save(TEST_TRANSACTION1);
        subscriptionService.createOrUpdateCurrentSubscriptionStatus(TELEGRAM_ID, SubscriptionStatus.PAID);
        telegramBotClient.sendMessageWithButtons("Твоя подписка успешно оплачена! 🎉\n\nВот ссылки для твоего удобства \uD83D\uDC47", TELEGRAM_ID);
        botMotherClient.sendPayload(TELEGRAM_ID);
        // Запрос
        var requestBuilder = post("/cloudpayments/success")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionRequest));

        // Выполнение запроса
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // Проверка, что транзакция и карта были сохранены
        verify(transactionService).save(TEST_TRANSACTION1);
        verify(cardService).saveCard(TEST_CARD1);
        // Проверка, что были вызваны другие методы
        verify(subscriptionService).createOrUpdateCurrentSubscriptionStatus(eq(TELEGRAM_ID), any());
        verify(telegramBotClient).sendMessageWithButtons(anyString(), eq(TELEGRAM_ID));
        verify(botMotherClient).sendPayload(eq(TELEGRAM_ID));
        verify(paymentService).updatePaymentStatus(TEST_PAYMENT.getId(), PaymentStatus.SUCCESS);
    }

    @Test
    @WithMockUser(username = "1234567890")
    void successWebhook_ShouldSaveTransactionAndSendTelegramMessage_WhenCardExists() throws Exception {
        var transactionRequest = createTestRequest();

        // Настройка мока для сервисов
        when(cardService.getCardByCardId(transactionRequest.getCardId())).thenReturn(TEST_CARD1); // Карта найдена
        when(paymentService.getLastPendingPayment(TELEGRAM_ID)).thenReturn(Optional.of(TEST_PAYMENT));

        paymentService.updatePaymentStatus(PAYMENT_ID, PaymentStatus.SUCCESS);
        transactionService.save(TEST_TRANSACTION1);
        subscriptionService.createOrUpdateCurrentSubscriptionStatus(TELEGRAM_ID, SubscriptionStatus.PAID);
        telegramBotClient.sendMessageWithButtons("Твоя подписка успешно оплачена! 🎉\n\nВот ссылки для твоего удобства \uD83D\uDC47", TELEGRAM_ID);
        botMotherClient.sendPayload(TELEGRAM_ID);

        // Запрос
        var requestBuilder = post("/cloudpayments/success")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionRequest));

        // Выполнение запроса
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // Проверка, что транзакция была сохранена
        verify(transactionService).save(TEST_TRANSACTION1);
        // Проверка, что карта не сохранялась
        verify(cardService, never()).saveCard(TEST_CARD1);
        // Проверка, что другие методы были вызваны
        verify(subscriptionService).createOrUpdateCurrentSubscriptionStatus(eq(TELEGRAM_ID), any());
        verify(telegramBotClient).sendMessageWithButtons(anyString(), eq(TELEGRAM_ID));
        verify(botMotherClient).sendPayload(eq(TELEGRAM_ID));
        verify(paymentService).updatePaymentStatus(TEST_PAYMENT.getId(), PaymentStatus.SUCCESS);
    }

    @Test
    @WithMockUser(username = "1234567890")
    void failWebhook_ShouldSaveTransactionAndDeactivateCard_WhenCardExists() throws Exception {
        var transactionRequest = createTestRequest();

        // Настройка мока для сервисов
        when(cardService.getCardByCardId(transactionRequest.getCardId())).thenReturn(TEST_CARD1);
        when(paymentService.getLastPendingPayment(TELEGRAM_ID)).thenReturn(Optional.of(TEST_PAYMENT));

        paymentService.updatePaymentStatus(PAYMENT_ID, PaymentStatus.FAILED);
        cardService.deactivateCard(transactionRequest.getCardId());
        transactionService.save(TEST_TRANSACTION1);

        // Запрос
        var requestBuilder = post("/cloudpayments/fail")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionRequest));
        // Выполнение запроса
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // Проверка, что транзакция была сохранена
        verify(transactionService).save(TEST_TRANSACTION1);
        // Проверка, что карта была деактивирована
        verify(cardService).deactivateCard(TEST_CARD1.getCardId());
        verify(paymentService).updatePaymentStatus(TEST_PAYMENT.getId(), PaymentStatus.FAILED);
    }

    @Test
    @WithMockUser(username = "1234567890")
    void failWebhook_ShouldSaveTransactionAndNotDeactivateCard_WhenCardDoesNotExist() throws Exception {
        var transactionRequest = createTestRequest();

        // Настройка мока для сервисов
        when(cardService.getCardByCardId(transactionRequest.getCardId())).thenReturn(null); // Карта не найдена
        when(paymentService.getLastPendingPayment(TELEGRAM_ID)).thenReturn(Optional.of(TEST_PAYMENT));

        paymentService.updatePaymentStatus(PAYMENT_ID, PaymentStatus.FAILED);
        transactionService.save(TEST_TRANSACTION1);

        // Запрос
        var requestBuilder = post("/cloudpayments/fail")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionRequest));

        System.out.println(objectMapper.writeValueAsString(transactionRequest));
        // Выполнение запроса
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // Проверка, что транзакция была сохранена
        verify(transactionService).save(TEST_TRANSACTION1);
        // Проверка, что деактивация карты не выполнялась
        verify(cardService, never()).deactivateCard(anyString());
        verify(paymentService).updatePaymentStatus(TEST_PAYMENT.getId(), PaymentStatus.FAILED);
    }

}