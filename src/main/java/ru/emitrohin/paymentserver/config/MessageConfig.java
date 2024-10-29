package ru.emitrohin.paymentserver.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "messages")
@Getter
@Setter
@RequiredArgsConstructor
public class MessageConfig {
    private String expirationNotification;
    private String subscriptionReminder;
}
