package ru.emitrohin.paymentserver.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.emitrohin.paymentserver.config.TelegramProperties;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TelegramBotClientTest {

    @InjectMocks
    private TelegramBotClient telegramBotClient;

    @Mock
    private TelegramProperties properties;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Logger logger;

    private static final Long TELEGRAM_ID = 1234567890L;

    @BeforeEach
    public void setUp() {
        var mockResponse = new ResponseEntity<>("{\"result\":{\"status\":\"left\"}}", HttpStatus.OK);
        lenient().when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockResponse);

        lenient().when(properties.botToken()).thenReturn("test-bot-token");
        lenient().when(properties.societyId()).thenReturn("-1002200073113");
    }

    @Test
    public void shouldRemoveUserFromTelegramGroup() {
        var jsonResponse = "{\"ok\":true, \"result\":\"User removed from telegram group\"}";
        var responseEntity = new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        telegramBotClient.removeFromTelegramGroup(TELEGRAM_ID);

        verify(restTemplate, times(1)).exchange(
                contains("/unbanChatMember"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        );
        var actualResponseBody = responseEntity.getBody();
        assertEquals(jsonResponse, actualResponseBody);
    }

    @Test
    public void shouldSendMessageToTelegramUser() throws JsonProcessingException {
        var jsonResponse = "{\"ok\":true, \"result\":{\"message_id\":12345}}";
        var responseEntity = new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        when(restTemplate.exchange(
                contains("/sendMessage"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class))
        ).thenReturn(responseEntity);

        telegramBotClient.sendMessage(TELEGRAM_ID, "Test message");

        verify(restTemplate, times(1)).exchange(
                contains("/sendMessage"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        );

        var captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(String.class));

        var capturedRequestEntity = captor.getValue();
        var requestBody = (Map<String, Object>) capturedRequestEntity.getBody();

        assertEquals(TELEGRAM_ID, requestBody.get("chat_id"));
        assertEquals("Test message", requestBody.get("text"));
    }


    @Test
    public void shouldVerifyUserStatusAsLeft() throws JsonProcessingException {
        var jsonResponseString = "{\"result\":{\"status\":\"left\"}}";
        var responseEntity = new ResponseEntity<>(jsonResponseString, HttpStatus.OK);

        when(restTemplate.exchange(
                contains("/getChatMember"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class))
        ).thenReturn(responseEntity);

        var mockJsonNode = new ObjectMapper().readTree(jsonResponseString);
        when(objectMapper.readTree(jsonResponseString)).thenReturn(mockJsonNode);

        telegramBotClient.verifyUserLeftGroup(TELEGRAM_ID);

        verify(restTemplate, times(1)).exchange(
                contains("/getChatMember"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        );
        logger.info(TELEGRAM_ID + " was kicked from the group");
        assertNotNull(responseEntity.getBody(), "Response body should not be null");

        var jsonResponse = objectMapper.readTree(jsonResponseString);
        assertEquals("left", jsonResponse.path("result").path("status").asText());
        verify(logger, times(1)).info(TELEGRAM_ID + " was kicked from the group");
    }


}
