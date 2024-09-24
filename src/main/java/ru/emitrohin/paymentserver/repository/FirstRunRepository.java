package ru.emitrohin.paymentserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.emitrohin.paymentserver.model.FirstRun;

import java.util.Optional;

public interface FirstRunRepository extends JpaRepository<FirstRun, Long> {
    Optional<FirstRun> findByTelegramId(long telegramId);
}