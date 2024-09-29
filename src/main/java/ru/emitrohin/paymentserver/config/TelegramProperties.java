package ru.emitrohin.paymentserver.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.telegram")
@Validated
public record TelegramProperties(
        @NotNull String botToken,
        @Positive long authExpirationTime,
        @NotNull String societyLink, //TODO validate URL
        @NotNull String societyId,
        @NotNull Long ownerId
) {
}
