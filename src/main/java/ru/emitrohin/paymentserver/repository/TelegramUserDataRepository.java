package ru.emitrohin.paymentserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.emitrohin.paymentserver.model.TelegramUserData;

import java.util.Optional;

public interface TelegramUserDataRepository extends JpaRepository<TelegramUserData, Long> {
    Optional<TelegramUserData> findByTelegramId(Long telegramId);
}