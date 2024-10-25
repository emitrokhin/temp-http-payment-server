package ru.emitrohin.paymentserver.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.emitrohin.paymentserver.config.TelegramProperties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(TelegramProperties.class)
public class TelegramBotClient {

    private final TelegramProperties properties;

    private final RestTemplate restTemplate;

    private final Environment environment;

    private final ObjectMapper objectMapper;

    //TODO проверять что пришло, повторять попытки и т  д
    public void sendMessageWithButtons(String text, long telegramId) {
        var testEnv = !environment.matchesProfiles("prod");
        var url = String.format("https://api.telegram.org/bot%s/%s/sendPhoto", properties.botToken(), testEnv ? "test" : "");
        var photoUrl = "https://s3.timeweb.cloud/d9448f39-76ed7011-ed95-4f47-a2d5-bc09db949407/success-payment.jpg";

        // Создаем кнопки
        var keyboard = new HashMap<>();
        keyboard.put("inline_keyboard",
                new Object[][]{
                        {createButtonWithUrl("Мой профиль", "https://mitrokhina.ru.tuna.am/profile", true)},
                        {createButtonWithUrl("Перейти в сообщество", properties.societyLink(), false)},
                        {createButtonWithUrl("Написать в поддержку", "https://t.me/fenomen_mitrohina_bot", false)}
                });

        // Создаем тело запроса
        var requestBody = new HashMap<>();
        requestBody.put("chat_id", telegramId);
        requestBody.put("caption", text); // Текст будет как подпись к фото
        requestBody.put("photo", photoUrl); // URL изображения
        requestBody.put("reply_markup", keyboard);

        var headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

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
        var testEnv = !environment.matchesProfiles("prod");
        var url = String.format("https://api.telegram.org/bot%s/%s/unbanChatMember", properties.botToken(), testEnv ? "test" : "");

        // Создаем тело запроса
        var requestBody = new HashMap<>();
        requestBody.put("chat_id", properties.societyId());
        requestBody.put("user_id", telegramId);

        var headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        var requestEntity = new HttpEntity<>(requestBody, headers);

        // Отправляем запрос
        var response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        response.getBody();
    }

    public void sendExpirationNotification(long telegramId) {
        String message = "Ваша подписка завершена, и вы были удалены из группы.";
        sendMessage(telegramId, message);
    }

    public void sendMessage(long telegramId, String message) {
        var testEnv = !environment.matchesProfiles("prod");
        var url = String.format("https://api.telegram.org/bot%s/%s/sendMessage", properties.botToken(), testEnv ? "test" : "");

        // Создаем тело запроса
        var requestBody = new HashMap<String, Object>();
        requestBody.put("chat_id", telegramId);
        requestBody.put("text", message);

        var headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        var requestEntity = new HttpEntity<>(requestBody, headers);

        // Отправляем запрос
        var response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        response.getBody();
    }

    public void verifyUserLeftGroup(long telegramId) {
        var testEnv = !environment.matchesProfiles("prod");
        var url = String.format("https://api.telegram.org/bot%s/%s/getChatMember", properties.botToken(), testEnv ? "test" : "");

        // Создаем тело запроса
        var requestBody = new HashMap<>();
        requestBody.put("chat_id", properties.societyId());
        requestBody.put("user_id", telegramId);

        var headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        var requestEntity = new HttpEntity<>(requestBody, headers);

        // Отправляем запрос
        var response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        // Обработка ответа
        var responseBody = response.getBody();

        JsonNode jsonResponse = null;
        try {
            jsonResponse = objectMapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String status = jsonResponse.path("result").path("status").asText();

        if (status.equals("left") || status.equals("kicked")) {
            System.out.println("Пользователь успешно покинул группу.");
        }
    }


}