package ru.emitrohin.paymentserver.IT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.emitrohin.paymentserver.model.SubscriptionStatus;
import ru.emitrohin.paymentserver.repository.TransactionRepository;
import ru.emitrohin.paymentserver.repository.SubscriptionRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//TODO тест корявый, он не проверяет как Servet мапит модель
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CloudpaymentsIntegrationTest {

    private final String X_CONTENT_HMAC = "";

    private final String CONTENT_HMAC = "";

    private final String VALID_IP = "92.63.206.131";

    private final long TRANSACTION_ID = 2136782003L;

    private final String CONTENT = "transactionId=" + TRANSACTION_ID + "&amount=2990.00&currency=RUB&paymentAmount=2990.00&paymentCurrency=RUB&" +
            "dateTime=2024-09-23T18:22:53&cardId=65698caa5d741b6af32b97c7&cardFirstSix=424242&cardLastFour=4242&cardType=Visa&" +
            "cardExpDate=04/24&testMode=true&status=Completed&operationType=Payment&gatewayName=Test&invoiceId=INV500056845420240923&" +
            "accountId=5000568454&subscriptionId=sc_3a69828e127fbea8e7d2a261c0600&email=test%40example.com&token=tk_c41f02328c73c74f3e937fa683be5";

    private final long TELEGRAM_ID = 5000568454L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Test
    @WithMockUser(username = "" + TELEGRAM_ID) // Добавляем мокированного пользователя для прохождения Spring Security
    void testSuccessWebhook() throws Exception {

        // Выполняем запрос через MockMvc
        //TODO обновить код заголовков после
        mockMvc.perform(post("/cloudpayments/success")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(CONTENT)
                        // Добавляем заголовки для HMAC
                        .header("X-Content-HMAC", X_CONTENT_HMAC)
                        .header("Content-HMAC", CONTENT_HMAC)
                        // Добавляем доверенный IP-адрес
                        .header("X-Forwarded-For", VALID_IP))
                .andExpect(status().isOk());

        // Проверяем, что транзакция была сохранена
        var transaction = transactionRepository.findByTransactionId(TRANSACTION_ID);
        assertThat(transaction).isPresent();
        assertThat(transaction.get().getTelegramId()).isEqualTo(TELEGRAM_ID);

        // Проверяем, что подписка была сохранена
        var subscription = subscriptionRepository.findFirstByTelegramIdAndSubscriptionDateBetween(TELEGRAM_ID, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        assertThat(subscription).isPresent();
        assertThat(subscription.get().getTelegramId()).isEqualTo(TELEGRAM_ID);
        assertThat(subscription.get().getSubscriptionStatus()).isEqualTo(SubscriptionStatus.PAID);
    }
}