package ru.emitrohin.paymentserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record TelegramUserRequest(
        @NotNull(message = "ID не должно быть пустым")
        @JsonProperty("id")
        long telegramId,

        @NotNull(message = "Имя не должно быть пустым")
        @JsonProperty("first_name")
        String firstName,

        @JsonProperty("last_name")
        String lastName,

        @JsonProperty("username")
        String username,

        @JsonProperty("language_code")
        String languageCode,

        @JsonProperty("allows_write_to_pm")
        boolean allowsWriteToPm,

        @JsonProperty("photo_url")
        String photoUrl
) {
}