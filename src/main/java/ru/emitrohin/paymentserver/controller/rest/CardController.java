package ru.emitrohin.paymentserver.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.emitrohin.paymentserver.model.Card;
import ru.emitrohin.paymentserver.service.CardService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CardController {

    private final CardService cardService;


    //TODO: возвращать коды(200, 404, 500)
    @PostMapping("/my/cards/{cardId}/set-primary")
    public ResponseEntity<Void> setPrimary(@PathVariable String cardId) {
        var telegramId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        cardService.setPrimary(telegramId, cardId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/my/cards/{cardId}/delete")
    public ResponseEntity<Void> deleteCard(@PathVariable String cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build(); // Возвращаем код 204
    }
}
