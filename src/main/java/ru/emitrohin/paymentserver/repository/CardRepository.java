package ru.emitrohin.paymentserver.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.emitrohin.paymentserver.model.Card;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findAllByTelegramId(long telegramId);
    Optional<Card> findByTelegramId(long telegramId);
    Optional<Card> findByCardId(String cardId);
    void deleteCardByCardId(String cardId);
    Optional<Card> findCardByToken(String token);
}
