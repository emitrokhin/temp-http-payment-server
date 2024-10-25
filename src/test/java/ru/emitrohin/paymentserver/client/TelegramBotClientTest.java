package ru.emitrohin.paymentserver.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.emitrohin.paymentserver.config.TelegramProperties;
import ru.emitrohin.paymentserver.security.TelegramPreAuthenticatedProcessingFilter;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WebMvcTest(TelegramBotClient.class)
public class TelegramBotClientTest {

    @Autowired
    private TelegramBotClient telegramBotClient;

    @MockBean
    private TelegramProperties properties;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private TelegramPreAuthenticatedProcessingFilter telegramPreAuthenticatedProcessingFilter;

    private static final Long TELEGRAM_ID = 1234567890L;

    @BeforeEach
    public void setUp() {
        var mockResponse = new ResponseEntity<>("", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockResponse);

        when(properties.botToken()).thenReturn("test-bot-token");
        when(properties.societyId()).thenReturn("-1002200073113");
    }

    @Test
    public void removeFromTelegramGroup_ShouldSendRequestToTelegramApi() {
        var responseEntity = new ResponseEntity<>("{}", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        telegramBotClient.removeFromTelegramGroup(TELEGRAM_ID);
        verify(restTemplate, times(1)).exchange(
                contains("/unbanChatMember"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    public void sendMessage_ShouldSendRequestToTelegramApi() {
        var responseEntity = new ResponseEntity<>("{}", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        telegramBotClient.sendMessage(TELEGRAM_ID, "Test message");

        verify(restTemplate, times(1)).exchange(
                contains("/sendMessage"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    public void verifyUserLeftGroup_ShouldCheckUserStatus() {
        var responseEntity = new ResponseEntity<>("{\"result\":{\"status\":\"left\"}}", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        telegramBotClient.verifyUserLeftGroup(TELEGRAM_ID);

        verify(restTemplate, times(1)).exchange(
                contains("/getChatMember"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        );
    }
}

