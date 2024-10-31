package ru.emitrohin.paymentserver.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.emitrohin.paymentserver.model.Card;
import ru.emitrohin.paymentserver.repository.CardRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;

    public List<Card> getAllCards(long telegramId) {
        return cardRepository.findAllByTelegramId(telegramId);
    }

    @Transactional
    public void setPrimary(long telegramId, String cardId) {
        var cards = cardRepository.findAllByTelegramId(telegramId);
        cards.forEach(card -> card.setIsPrimary(false));
        var card = cardRepository.findByCardId(cardId);
        card.get().setIsPrimary(true);
        cardRepository.save(card.get());
    }

    @Transactional
    public void deleteCard(String cardId) {
        cardRepository.deleteCardByCardId(cardId);
    }

    public void saveCard(Card card) {
        cardRepository.save(card);
    }

    @Transactional
    public void deactivateCard(String cardId) {
        var card = cardRepository.findByCardId(cardId).orElseThrow();
        card.setIsActive(false);
        cardRepository.save(card);
    }

    public Card getCardByCardId(String cardId) {
        return cardRepository.findByCardId(cardId).orElse(null);
    }

    @Transactional
    public void deactivatePrimaryForAllCards(long telegramId) {
        var cards = cardRepository.findAllByTelegramId(telegramId);
        cards.forEach(card -> {
            if (card.getIsPrimary()) {
                card.setIsPrimary(false);
                cardRepository.save(card);
            }
        });
    }

    public Card getPrimaryCard(long telegramId) {
        return cardRepository.findByTelegramIdAndIsPrimaryTrue(telegramId).orElseThrow();
    }

}