package ru.emitrohin.paymentserver.IT;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@WireMockTest(httpPort = 8089)
public class BugsnagSendRepostIntegrationTest {

    @Autowired
    private ObjectMapper jacksonObjectMapper;

    private static final Logger logger = LoggerFactory.getLogger(BugsnagSendRepostIntegrationTest.class);

    public static final String TEST_EXCEPTION = "Test exception";

    public static final String TEST_URL = "/";

    @Test
    public void testErrorSentToBugsnag() throws JsonProcessingException {

        // конфиг
        stubFor(post(WireMock.urlPathEqualTo(TEST_URL))
                .willReturn(WireMock.aResponse().withStatus(200)));

        // пишем
        logger.error(TEST_EXCEPTION, new RuntimeException(TEST_EXCEPTION));

        // Ожидаем, пока запрос не будет отправлен в течении 3‑х секунд
        await().atMost(3, TimeUnit.SECONDS)
               .untilAsserted(() -> verify(postRequestedFor(urlEqualTo(TEST_URL))));

        // Проверим, что запросы есть
        var requests = findAll(postRequestedFor(urlEqualTo(TEST_URL)));
        assertThat(requests).isNotEmpty();

        // Проверяем конкретные поля в JSON
        var jsonNode = jacksonObjectMapper.readTree(requests.getFirst().getBodyAsString());
        var errorClass = jsonNode.get("events")
                .get(0)
                .get("exceptions")
                .get(0)
                .get("errorClass")
                .asText();
        var message = jsonNode.get("events")
                .get(0)
                .get("exceptions")
                .get(0).get("message").
                asText();
        assertThat(errorClass).isEqualTo(RuntimeException.class.getName());
        assertThat(message).isEqualTo(TEST_EXCEPTION);
    }
}
