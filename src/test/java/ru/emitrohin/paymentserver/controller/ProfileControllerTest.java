package ru.emitrohin.paymentserver.controller;

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
import ru.emitrohin.paymentserver.dto.TransactionResponse;
import ru.emitrohin.paymentserver.dto.mapper.TransactionMapper;
import ru.emitrohin.paymentserver.dto.profile.ProfileUpdateDTO;
import ru.emitrohin.paymentserver.model.Profile;
import ru.emitrohin.paymentserver.model.Transaction;
import ru.emitrohin.paymentserver.service.FirstRunService;
import ru.emitrohin.paymentserver.service.ProfileService;
import ru.emitrohin.paymentserver.service.TransactionService;
import ru.emitrohin.paymentserver.dto.mapper.ProfileMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private FirstRunService firstRunService;

    @MockBean
    private ProfileMapper profileMapper;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private TransactionMapper transactionMapper;

    @Test
    @WithMockUser(username = "1234567890")
    void getUser_ProfileExists_ShouldReturnProfilePageWithTransactions() throws Exception {
        // Создаем тестовые объекты профиля
        var profile = new Profile();
        profile.setTelegramId(1234567890L);
        profile.setFirstName("Pavel");
        profile.setLastName("Zaytsev");
        profile.setPhone("+1234567890");
        profile.setEmail("pavel@zaytsev.com");
        profile.setDateOfBirth(LocalDate.of(2004, 4, 20));
        profile.setCity("Samara");
        profile.setProfession("Developer");

        // Создаем DTO для профиля
        var profileDTO = new ProfileUpdateDTO(
                "Pavel",
                "Zaytsev",
                "+1234567890",
                "pavel@zaytsev.com",
                LocalDate.of(2004, 4, 20),
                "Samara",
                "Developer");

        // Создаем тестовые транзакции
        var transaction1 = new Transaction();
        transaction1.setAmount(new BigDecimal("100"));
        transaction1.setDateTime(LocalDateTime.now());
        transaction1.setCurrency("RUB");

        var transaction2 = new Transaction();
        transaction2.setAmount(new BigDecimal("200"));
        transaction2.setDateTime(LocalDateTime.now().minusDays(1));
        transaction2.setCurrency("RUB");

        // Настройка мока
        when(profileService.findByTelegramId(1234567890L)).thenReturn(Optional.of(profile));
        when(transactionService.getAllTransactions(anyLong())).thenReturn(List.of(transaction1, transaction2));
        when(transactionMapper.toTransactionResponse(transaction1)).thenReturn(new TransactionResponse(transaction1.getAmount(), transaction1.getDateTime(), transaction1.getCurrency()));
        when(transactionMapper.toTransactionResponse(transaction2)).thenReturn(new TransactionResponse(transaction2.getAmount(), transaction2.getDateTime(), transaction2.getCurrency()));
        when(firstRunService.findFirstRun(1234567890L)).thenReturn(Optional.empty());
        when(profileMapper.createUpdateResponse(profile)).thenReturn(profileDTO);

        // Выполнение запроса
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("firstRun"))
                .andExpect(model().attributeExists("profileForm"))
                .andExpect(model().attributeExists("transactions"))
                .andExpect(model().attribute("transactions", List.of(
                        new TransactionResponse(transaction1.getAmount(), transaction1.getDateTime(), transaction1.getCurrency()),
                        new TransactionResponse(transaction2.getAmount(), transaction2.getDateTime(), transaction2.getCurrency())
                )));
    }

    @Test
    @WithMockUser(username = "1234567890")
    void getUser_ProfileNotFound_ShouldRedirectToHomePage() throws Exception {
        // Мокируем SecurityContext
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("1234567890"); // Пример Telegram ID
        SecurityContextHolder.setContext(securityContext);

        // Мокируем, что профиль не найден
        when(profileService.findByTelegramId(1234567890L)).thenReturn(Optional.empty());

        // Выполняем GET-запрос и проверяем редирект
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }
}
