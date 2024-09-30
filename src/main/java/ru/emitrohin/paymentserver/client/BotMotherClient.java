package ru.emitrohin.paymentserver.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class BotMotherClient {

    private final String BOTMOTHER_WEBHOOK = "https://app.botmother.com/api/bot/action/KlV0UI_fH/DeBLB0VCmDOT9DQC4DrB_D3xDuD1DICeC0BfDBD_BLCF2D2CYHCkDoCtBJCFdD1C";

    private final RestTemplate restTemplate;

    public void sendPayload(long telegramId) {

        // Создаем тело запроса
        var requestBody = new HashMap<>();
        requestBody.put("platform", "tg");
        requestBody.put("users", new Object[]{telegramId});

        var headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        var requestEntity = new HttpEntity<>(requestBody, headers);

        restTemplate.exchange(BOTMOTHER_WEBHOOK, HttpMethod.POST, requestEntity, String.class);
    }
}