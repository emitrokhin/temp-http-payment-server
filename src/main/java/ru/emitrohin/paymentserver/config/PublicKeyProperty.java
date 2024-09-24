package ru.emitrohin.paymentserver.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app")
@Validated
public record PublicKeyProperty(@NotNull String publicKey) {
}
