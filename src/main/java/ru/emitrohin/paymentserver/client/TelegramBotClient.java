package ru.emitrohin.paymentserver.client;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.emitrohin.paymentserver.config.TelegramProperties;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(TelegramProperties.class)
public class TelegramBotClient {

    private final TelegramProperties properties;

    private final RestTemplate restTemplate;

    private final Environment environment;

    /**
     * Метод для отправки сообщения с кнопками
     */
    //TODO проверять что пришло, повторять попытки и т  д
    public void sendMessageWithButtons(String text, long telegramId) {
        var testEnv = !environment.matchesProfiles("prod");
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
}