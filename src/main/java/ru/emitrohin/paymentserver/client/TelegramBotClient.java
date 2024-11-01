package ru.emitrohin.paymentserver.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.emitrohin.paymentserver.config.CheckTestEnvironment;
import ru.emitrohin.paymentserver.config.MessageConfig;
import ru.emitrohin.paymentserver.config.TelegramProperties;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(TelegramProperties.class)
public class TelegramBotClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TelegramProperties properties;

    private final RestTemplate restTemplate;

    private final Environment environment;

    private final ObjectMapper objectMapper;

    private final MessageConfig messageConfig;

    private final CheckTestEnvironment checkTestEnvironment;

    private Boolean testEnv;

    @PostConstruct
    public void init() {
        this.testEnv = checkTestEnvironment.isTestEnvironment();
    }

    /**
     * Метод для отправки сообщения с кнопками
     */
    //TODO проверять что пришло, повторять попытки и т  д
    public void sendMessageWithButtons(String text, long telegramId) {
        var url = String.format("https://api.telegram.org/bot%s/%s/sendPhoto", properties.botToken(), testEnv ? "test" : "");
        var photoUrl = "https://s3.timeweb.cloud/d9448f39-76ed7011-ed95-4f47-a2d5-bc09db949407/success-payment.jpg";

        // Создаем кнопки
        var keyboard = new HashMap<>();
        keyboard.put("inline_keyboard",
                new Object[][] {
                        { createButtonWithUrl("Мой профиль", "https://mitrokhina.ru.tuna.am/profile", true) },
                        { createButtonWithUrl("Перейти в сообщество", properties.societyLink(), false) },
                        { createButtonWithUrl("Написать в поддержку", "https://t.me/fenomen_mitrohina_bot", false) }
                });

        // Создаем тело запроса
        var requestBody = new HashMap<>();
        requestBody.put("chat_id", telegramId);
        requestBody.put("caption", text); // Текст будет как подпись к фото
        requestBody.put("photo", photoUrl); // URL изображения
        requestBody.put("reply_markup", keyboard);

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        var requestEntity = new HttpEntity<>(requestBody, headers);

        // Отправляем запрос
        var response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        response.getBody();
    }

    private Map<String, Object> createButtonWithUrl(String text, String url, boolean isWebApp) {
        Map<String, Object> button = new HashMap<>();
        button.put("text", text);

        if (isWebApp) {
            // Если это мини-приложение, используем web_app
            var webApp = new HashMap<>();
            webApp.put("url", url);
            button.put("web_app", webApp);
        } else {
            // Если это обычная ссылка
            button.put("url", url);
        }

        return button;
    }

    public void removeFromTelegramGroup(Long telegramId) {
        var url = String.format("https://api.telegram.org/bot%s/%s/unbanChatMember", properties.botToken(), testEnv ? "test" : "");

        var requestBody = new HashMap<>();
        requestBody.put("chat_id", properties.societyId());
        requestBody.put("user_id", telegramId);

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        var requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            var response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            var objectMapper = new ObjectMapper();
            var jsonResponse = objectMapper.readTree(response.getBody());

            if (jsonResponse.path("ok").asBoolean() && jsonResponse.path("result").isObject()) {
                logger.info("User with ID {} has been successfully unbanned from the group.", telegramId);
            } else {
                logger.error("Failed to unban user with ID {}: {}", telegramId, jsonResponse.path("description").asText());
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse response for user ID {}: {}", telegramId, e.getMessage());
        } catch (Exception e) {
            logger.error("An error occurred while trying to unban user with ID {}: {}", telegramId, e.getMessage());
        }
    }


    public void sendExpirationNotification(long telegramId) {
        sendMessage(telegramId, messageConfig.getSubscriptionIsExpiredNotification());
    }

    public void sendMessage(long telegramId, String message) {
        var url = String.format("https://api.telegram.org/bot%s/%s/sendMessage", properties.botToken(), testEnv ? "test" : "");

        var requestBody = new HashMap<String, Object>();
        requestBody.put("chat_id", telegramId);
        requestBody.put("text", message);

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        var requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            var response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            var objectMapper = new ObjectMapper();
            var jsonResponse = objectMapper.readTree(response.getBody());

            if (jsonResponse.path("ok").asBoolean() && jsonResponse.path("result").isObject()) {
                logger.info("Message sent to user with ID {}: {}", telegramId, message);
            } else {
                logger.error("Failed to send message to user with ID {}: {}", telegramId, jsonResponse.path("description").asText());
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse response for user ID {}: {}", telegramId, e.getMessage());
        } catch (Exception e) {
            logger.error("An error occurred while trying to send a message to user with ID {}: {}", telegramId, e.getMessage());
        }
    }

}