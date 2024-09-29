package ru.emitrohin.paymentserver.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.cloudpayments")
@Validated
//TODO move password and pk to env
public record CloudpaymentsProperties(
        @NotNull String publicKey,
        @NotNull String password) {
}
