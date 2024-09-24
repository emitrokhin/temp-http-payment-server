package ru.emitrohin.paymentserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.emitrohin.paymentserver.model.PersonalData;

import java.util.Optional;

public interface PersonalDataRepository extends JpaRepository<PersonalData, Long> {

    Optional<PersonalData> findByTelegramId(long telegramId);
}