package ru.emitrohin.paymentserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.emitrohin.paymentserver.model.Profile;

import java.util.Optional;

public interface PersonalDataRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByTelegramId(long telegramId);
}