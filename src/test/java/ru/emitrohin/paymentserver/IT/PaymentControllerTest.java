package ru.emitrohin.paymentserver.IT;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;
import org.springframework.web.client.RestTemplate;
import ru.emitrohin.paymentserver.config.CloudpaymentsProperties;
import ru.emitrohin.paymentserver.controller.PaymentController;
import ru.emitrohin.paymentserver.dto.cloudpayments.CloudpaymentsRequest;
import ru.emitrohin.paymentserver.dto.profile.ProfilePaymentDTO;
import ru.emitrohin.paymentserver.model.Payment;
import ru.emitrohin.paymentserver.model.TelegramUserData;
import ru.emitrohin.paymentserver.security.TelegramPreAuthenticatedProcessingFilter;
import ru.emitrohin.paymentserver.service.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TelegramUserDataService telegramUserDataService;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private SubscriptionService subscriptionService;

    @MockBean
    private FirstRunService firstRunService;

    @MockBean
    private CloudpaymentsProperties property;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private RestTemplate restTemplate;

    @InjectMocks
    private PaymentController paymentController;

    @MockBean
    private TelegramPreAuthenticatedProcessingFilter telegramPreAuthenticatedProcessingFilter;

    private static final long TELEGRAM_ID = 1234567890L;

    private static final CloudpaymentsRequest REQUEST = new CloudpaymentsRequest(
            1L,                                   // TransactionId
            null,            // Amount
            null,                                // Currency
            new BigDecimal("100.00"),            // PaymentAmount
            "RUB",                                // PaymentCurrency
            "Charge",                             // OperationType
            "INV-123456",                         // InvoiceId
            "123456789",                          // AccountId
            "SUBS-789012",                        // SubscriptionId
            "test@example.com",                   // Email
            LocalDateTime.now(),                  // DateTime
            "Insufficient funds",                  // Reason
            101,                                   // ReasonCode
            "CARD-12345",                         // CardId
            123456,                               // CardFirstSix
            7890,                                 // CardLastFour
            "Visa",                               // CardType
            "12/25",                              // CardExpDate
            "Some Bank",                          // Issuer
            null,                       // Token
            (byte) 0,                             // TestMode
            "Success",                            // Status
            "Card"                                // PaymentMethod
    );

    private static final ProfilePaymentDTO VALID_PAYMENTDTO = new ProfilePaymentDTO("John", "Doe", "+1234567890", "test@example.com", TELEGRAM_ID);
    private static final ProfilePaymentDTO INVALID_PAYMENTDTO = new ProfilePaymentDTO("John", "Doe", "1234567890", "test@example.com", TELEGRAM_ID);

    @Test
    @WithMockUser(username = "1234567890")
    public void chargeToken_ShouldReturnOk_WhenPaymentIsSuccessful() throws Exception {

        REQUEST.setAmount(new BigDecimal(100));
        REQUEST.setCurrency("RUB");
        REQUEST.setToken("sample-token");

        when(property.publicKey()).thenReturn("yourPublicKey");
        when(property.password()).thenReturn("yourApiSecret");
        when(paymentService.createPayment(TELEGRAM_ID)).thenReturn(new Payment());

        when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), any(Class.class)))
                .thenAnswer(invocation -> {
                    String url = invocation.getArgument(0);
                    HttpEntity<?> entity = invocation.getArgument(1);

                    assert url.equals("https://api.cloudpayments.ru/payments/tokens/charge");

                    var headers = entity.getHeaders();
                    var authHeader = headers.getFirst("Authorization");
                    assert authHeader != null && authHeader.startsWith("Basic ");

                    return new ResponseEntity<>("{}", HttpStatus.OK);
                });

        mockMvc.perform(post("/tokens/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":100,\"currency\":\"RUB\",\"token\":\"sample-token\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "1234567890")
    public void pay_ShouldRedirectToSuccess_WhenSubscriptionIsPaid() throws Exception {
        when(telegramUserDataService.findByTelegramId(TELEGRAM_ID)).thenReturn(Optional.of(new TelegramUserData()));
        when(subscriptionService.hasPaidSubscription(TELEGRAM_ID)).thenReturn(true);

        mockMvc.perform(post("/pay")
                        .flashAttr("profilePaymentForm", VALID_PAYMENTDTO))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/success"));

        verify(paymentService, never()).createPayment(TELEGRAM_ID);
    }


    @Test
    @WithMockUser(username = "1234567890")
    public void pay_ShouldRedirectToIndex_WhenUserNotFound() throws Exception {
        when(telegramUserDataService.findByTelegramId(TELEGRAM_ID)).thenReturn(Optional.empty());

        mockMvc.perform(post("/pay")
                        .flashAttr("profilePaymentForm", VALID_PAYMENTDTO))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));
    }

    @Test
    @WithMockUser(username = "1234567890")
    public void pay_ShouldRedirectToIndex_WhenBindingErrors() throws Exception {
        when(telegramUserDataService.findByTelegramId(TELEGRAM_ID)).thenReturn(Optional.of(new TelegramUserData()));
        when(subscriptionService.hasPaidSubscription(TELEGRAM_ID)).thenReturn(false);
        when(subscriptionService.findCurrentSubscription(TELEGRAM_ID)).thenReturn(Optional.empty());
        when(paymentService.createPayment(TELEGRAM_ID)).thenReturn(new Payment());

        mockMvc.perform(post("/pay")
                        .flashAttr("profilePaymentForm", INVALID_PAYMENTDTO))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));
    }


    @Test
    @WithMockUser(username = "1234567890")
    public void pay_ShouldReturnPayView_WhenPaymentIsSuccessful() throws Exception {
        when(telegramUserDataService.findByTelegramId(TELEGRAM_ID)).thenReturn(Optional.of(new TelegramUserData()));
        when(subscriptionService.hasPaidSubscription(TELEGRAM_ID)).thenReturn(false);
        when(subscriptionService.findCurrentSubscription(TELEGRAM_ID)).thenReturn(Optional.empty());
        when(paymentService.createPayment(TELEGRAM_ID)).thenReturn(new Payment());

        mockMvc.perform(post("/pay")
                        .flashAttr("profilePaymentForm", VALID_PAYMENTDTO))
                .andExpect(status().isOk())
                .andExpect(view().name("pay"));

        verify(profileService).saveOrUpdateProfilePayment(TELEGRAM_ID, VALID_PAYMENTDTO);
        verify(subscriptionService).createPendingSubscription(TELEGRAM_ID);
    }
}
