package ru.emitrohin.paymentserver.controller.rest;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.emitrohin.paymentserver.dto.mapper.CardMapper;
import ru.emitrohin.paymentserver.dto.profile.ProfileUpdateDTO;
import ru.emitrohin.paymentserver.model.Card;
import ru.emitrohin.paymentserver.model.Profile;
import ru.emitrohin.paymentserver.service.CardService;
import ru.emitrohin.paymentserver.service.ProfileService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private CardMapper cardMapper;

    private static final Long TELEGRAM_ID = 1234567890L;
    private static final Profile TEST_PROFILE = createTestProfile();
    private static final Card TEST_CARD1 = createTestCard(3055, "05/55", true,
            true, "VISA", "12345");
    private static final Card TEST_CARD2 = createTestCard(2222, "03/33", true,
            false, "MASTERCARD", "54321");

    private static Card createTestCard(int cardLastFour, String cardExpDate, Boolean isActive, Boolean isPrimary,
                                       String cardType, String cardId) {
        var card = new Card();
        card.setCardLastFour(cardLastFour);
        card.setCardExpDate(cardExpDate);
        card.setIsActive(isActive);
        card.setIsPrimary(isPrimary);
        card.setCardType(cardType);
        card.setCardId(cardId);
        return card;
    }

    private static Profile createTestProfile() {
        var profile = new Profile();
        profile.setTelegramId(TELEGRAM_ID);
        profile.setFirstName("Pavel");
        profile.setLastName("Zaytsev");
        profile.setPhone("+1234567890");
        profile.setEmail("pavel@zaytsev.com");
        profile.setDateOfBirth(LocalDate.of(2004, 4, 20));
        profile.setCity("Samara");
        profile.setProfession("Developer");
        return profile;
    }

    private static ProfileUpdateDTO createProfileUpdateDTO() {
        return new ProfileUpdateDTO(
                "Pavel",
                "Zaytsev",
                "+1234567890",
                "pavel@zaytsev.com",
                LocalDate.of(2004, 4, 20),
                "Samara",
                "Developer"
        );
    }

    @Test
    @WithMockUser(username = "1234567890")
    void setPrimary_CardExists_ShouldReturnOk() throws Exception {
        // Задаем ожидаемое состояние для карт
        var secondaryCard = createTestCard(TEST_CARD1.getCardLastFour(), TEST_CARD1.getCardExpDate(),
                TEST_CARD1.getIsActive(), TEST_CARD1.getIsPrimary(), TEST_CARD1.getCardType(), TEST_CARD1.getCardId());

        // Мокаем поведение сервиса
        when(profileService.findByTelegramId(TELEGRAM_ID)).thenReturn(Optional.of(TEST_PROFILE));
        when(cardService.getAllCards(TELEGRAM_ID)).thenReturn(List.of(TEST_CARD1, secondaryCard));
        when(cardService.getCardByCardId(TEST_CARD1.getCardId())).thenReturn(TEST_CARD1);

        // Выполнение запроса
        mockMvc.perform(post("/api/my/cards/{cardId}/set-primary", TEST_CARD1.getCardId()))
                .andExpect(status().isOk());

        // Проверка, что метод setPrimary был вызван с ожидаемыми параметрами
        verify(cardService).setPrimary(TELEGRAM_ID, TEST_CARD1.getCardId());

        // Проверка, что статус карты обновился
        var updatedCards = cardService.getAllCards(TELEGRAM_ID);
        assertTrue(updatedCards.stream().anyMatch(card -> card.getCardId().equals(TEST_CARD1.getCardId()) && card.getIsPrimary()));
        assertTrue(updatedCards.stream().noneMatch(card -> !card.getCardId().equals(TEST_CARD1.getCardId()) && card.getIsPrimary()));
    }

    @Test
    @WithMockUser(username = "1234567890")
    void deleteCard_CardExists_ShouldReturnNoContent() throws Exception {
        // Задаем ожидаемое состояние для карт
        var secondaryCard = TEST_CARD2; // Вторая карта, которую мы не будем удалять

        // Мокаем поведение сервиса, чтобы вернуть список из двух карт
        when(cardService.getAllCards(TELEGRAM_ID)).thenReturn(List.of(TEST_CARD1, secondaryCard));

        // Выполнение запроса на удаление карты
        mockMvc.perform(delete("/api/my/cards/{cardId}/delete", TEST_CARD1.getCardId()))
                .andExpect(status().isNoContent());

        // Проверка, что метод deleteCard был вызван с ожидаемыми параметрами
        verify(cardService).deleteCard(TEST_CARD1.getCardId());

        // Обновление мока для получения списка карт после удаления
        when(cardService.getAllCards(TELEGRAM_ID)).thenReturn(List.of(secondaryCard)); // Карта TEST_CARD1 была удалена

        // Проверка, что карта действительно удалена
        var updatedCards = cardService.getAllCards(TELEGRAM_ID);
        assertTrue(updatedCards.stream().noneMatch(card -> card.getCardId().equals(TEST_CARD1.getCardId())),
                "Card should be deleted and not found in the list");

        // Дополнительная проверка, что в списке осталась только вторая карта
        assertTrue(updatedCards.stream().anyMatch(card -> card.getCardId().equals(secondaryCard.getCardId())),
                "Secondary card should still be present in the list");
    }
}
