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
import ru.emitrohin.paymentserver.model.FirstRun;
import ru.emitrohin.paymentserver.model.Profile;
import ru.emitrohin.paymentserver.model.Transaction;
import ru.emitrohin.paymentserver.service.FirstRunService;
import ru.emitrohin.paymentserver.service.ProfileService;
import ru.emitrohin.paymentserver.service.TransactionService;
import ru.emitrohin.paymentserver.dto.mapper.ProfileMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    private static final Long TELEGRAM_ID = 1234567890L;
    private static final Profile TEST_PROFILE = createTestProfile();
    private static final ProfileUpdateDTO TEST_PROFILE_DTO = createProfileUpdateDTO();
    private static final Transaction TEST_TRANSACTION1 = createTestTransaction(BigDecimal.valueOf(100), LocalDateTime.now(), "RUB");
    private static final Transaction TEST_TRANSACTION2 = createTestTransaction(BigDecimal.valueOf(200), LocalDateTime.now().minusDays(1), "RUB");

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

    private static Transaction createTestTransaction(BigDecimal amount, LocalDateTime dateTime, String currency) {
        var transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDateTime(dateTime);
        transaction.setCurrency(currency);
        return transaction;
    }

    @Test
    @WithMockUser(username = "1234567890")
    void getUser_ProfileExists_ShouldReturnProfilePageWithTransactions() throws Exception {
        // Настройка мока
        when(profileService.findByTelegramId(TELEGRAM_ID)).thenReturn(Optional.of(TEST_PROFILE));
        when(transactionService.getAllTransactions(anyLong())).thenReturn(List.of(TEST_TRANSACTION1, TEST_TRANSACTION2));
        when(transactionMapper.toTransactionResponse(TEST_TRANSACTION1)).thenReturn(new TransactionResponse(TEST_TRANSACTION1.getAmount(), TEST_TRANSACTION1.getDateTime(), TEST_TRANSACTION1.getCurrency()));
        when(transactionMapper.toTransactionResponse(TEST_TRANSACTION2)).thenReturn(new TransactionResponse(TEST_TRANSACTION2.getAmount(), TEST_TRANSACTION2.getDateTime(), TEST_TRANSACTION2.getCurrency()));
        when(firstRunService.findFirstRun(TELEGRAM_ID)).thenReturn(empty());
        when(profileMapper.createUpdateResponse(TEST_PROFILE)).thenReturn(TEST_PROFILE_DTO);

        // Выполнение запроса
        var result = mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("firstRun"))
                .andExpect(model().attributeExists("profileForm"))
                .andExpect(model().attributeExists("transactions"))
                .andReturn();

        // Получение списка транзакций из результата
        var transactions = (List<TransactionResponse>) result.getModelAndView().getModel().get("transactions");

        // Проверка списка транзакций с использованием AssertJ
        assertThat(transactions)
                .hasSize(2)
                .extracting(TransactionResponse::amount)
                .containsExactlyInAnyOrder(TEST_TRANSACTION1.getAmount(), TEST_TRANSACTION2.getAmount());

        assertThat(transactions)
                .extracting(TransactionResponse::dateTime)
                .containsExactlyInAnyOrder(TEST_TRANSACTION1.getDateTime(), TEST_TRANSACTION2.getDateTime());

        assertThat(transactions)
                .extracting(TransactionResponse::currency)
                .containsExactlyInAnyOrder(TEST_TRANSACTION1.getCurrency(), TEST_TRANSACTION2.getCurrency());
    }

    @Test
    @WithMockUser(username = "1234567890")
    void getUser_ProfileExists_NoTransactions_ShouldReturnEmptyTransactions() throws Exception {
        // Настройка мока
        when(profileService.findByTelegramId(TELEGRAM_ID)).thenReturn(Optional.of(TEST_PROFILE));
        when(transactionService.getAllTransactions(anyLong())).thenReturn(emptyList()); // Имитация отсутствия транзакций
        when(firstRunService.findFirstRun(TELEGRAM_ID)).thenReturn(empty());
        when(profileMapper.createUpdateResponse(TEST_PROFILE)).thenReturn(TEST_PROFILE_DTO);

        // Выполнение запроса
        var result = mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("firstRun"))
                .andExpect(model().attributeExists("profileForm"))
                .andExpect(model().attributeExists("transactions"))
                .andReturn();

        // Получение списка транзакций из результата
        var transactions = (List<TransactionResponse>) result.getModelAndView().getModel().get("transactions");

        // Проверка списка транзакций с использованием AssertJ
        assertThat(transactions)
                .isNotNull()
                .isEmpty(); // Проверяем, что список транзакций пустой
    }

    @Test
    @WithMockUser(username = "1234567890")
    void getUser_FirstRun_ShouldReturnProfilePageWithLimitedFields() throws Exception {
        // Создаем запись первого запуска
        var firstRun = new FirstRun();
        firstRun.setTelegramId(TELEGRAM_ID);

        // Настройка мока
        when(profileService.findByTelegramId(TELEGRAM_ID)).thenReturn(Optional.of(TEST_PROFILE));
        when(firstRunService.findFirstRun(TELEGRAM_ID)).thenReturn(Optional.of(firstRun)); // Симулируем первый запуск
        when(profileMapper.createUpdateResponse(TEST_PROFILE)).thenReturn(createLimitedProfileDTO()); // Возвращаем DTO с ограниченными полями

        // Выполнение запроса
        var result = mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("firstRun")) // Проверяем наличие флага первого запуска
                .andExpect(model().attributeExists("profileForm"))
                .andExpect(model().attributeExists("transactions")); // Проверяем наличие атрибута "transactions"

// Получаем атрибут "transactions" из модели
        List<?> transactions = (List<?>) result.andReturn().getModelAndView().getModel().get("transactions");

// Проверяем, что это пустой список
        assertThat(transactions).isNotNull(); // Убедитесь, что он не null
        assertThat(transactions).isEmpty(); // Проверяем, что список пустой

        // Получаем DTO профиля из результата
        var profileForm = result.andReturn().getModelAndView().getModel().get("profileForm");

        // Проверяем поля профиля с помощью AssertJ
        assertThat(profileForm).isInstanceOf(ProfileUpdateDTO.class);
        var dto = (ProfileUpdateDTO) profileForm;

        assertThat(dto)
                .extracting(ProfileUpdateDTO::firstName, ProfileUpdateDTO::lastName, ProfileUpdateDTO::phone, ProfileUpdateDTO::email)
                .containsExactly(TEST_PROFILE.getFirstName(), TEST_PROFILE.getLastName(), TEST_PROFILE.getPhone(), TEST_PROFILE.getEmail());

        assertThat(dto.dateOfBirth()).isNull(); // Дата рождения не передается
        assertThat(dto.city()).isNull(); // Город не передается
        assertThat(dto.profession()).isNull(); // Профессия не передается
    }

    private ProfileUpdateDTO createLimitedProfileDTO() {
        return new ProfileUpdateDTO(
                "Pavel",
                "Zaytsev",
                "+1234567890",
                "pavel@zaytsev.com",
                null,  // Дата рождения не передается
                null,  // Город не передается
                null   // Профессия не передается
        );
    }

    @Test
    @WithMockUser(username = "1234567890")
    void getUser_ProfileNotFound_ShouldRedirectToHomePage() throws Exception {
        // Мокируем SecurityContext
        var authentication = Mockito.mock(Authentication.class);
        var securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("1234567890");
        SecurityContextHolder.setContext(securityContext);

        // Мокируем, что профиль не найден
        when(profileService.findByTelegramId(TELEGRAM_ID)).thenReturn(empty());

        // Выполняем GET-запрос и проверяем редирект
        var result = mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        // Проверяем ModelAndView
        var modelAndView = result.getModelAndView();
        assertThat(modelAndView).isNotNull();
        assertThat(modelAndView.getViewName()).isEqualTo("redirect:/");
        assertThat(modelAndView.getModel()).isEmpty();
    }

    @Test
    @WithMockUser(username = "1234567890")
    void getUser_ProfilePage_ShouldDisplayAllFields() throws Exception {
        // Настройка мока
        when(profileService.findByTelegramId(TELEGRAM_ID)).thenReturn(Optional.of(TEST_PROFILE));
        when(transactionService.getAllTransactions(anyLong())).thenReturn(List.of(TEST_TRANSACTION1, TEST_TRANSACTION2));
        when(transactionMapper.toTransactionResponse(TEST_TRANSACTION1)).thenReturn(new TransactionResponse(TEST_TRANSACTION1.getAmount(), TEST_TRANSACTION1.getDateTime(), TEST_TRANSACTION1.getCurrency()));
        when(transactionMapper.toTransactionResponse(TEST_TRANSACTION2)).thenReturn(new TransactionResponse(TEST_TRANSACTION2.getAmount(), TEST_TRANSACTION2.getDateTime(), TEST_TRANSACTION2.getCurrency()));
        when(firstRunService.findFirstRun(TELEGRAM_ID)).thenReturn(empty());
        when(profileMapper.createUpdateResponse(TEST_PROFILE)).thenReturn(TEST_PROFILE_DTO);

        // Выполнение запроса
        var result = mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("firstRun"))
                .andExpect(model().attributeExists("profileForm"))
                .andExpect(model().attributeExists("transactions"))
                .andReturn();

        // Получение HTML-контента
        var content = result.getResponse().getContentAsString();

        // Форматирование дат транзакций
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

        // Проверка содержимого
        assertThat(content).contains("Подписка");
        assertThat(content).contains(TEST_TRANSACTION1.getAmount().toString() + " " + TEST_TRANSACTION1.getCurrency());
        assertThat(content).contains(TEST_TRANSACTION1.getDateTime().format(formatter)); // Используем formatter
        assertThat(content).contains(TEST_TRANSACTION2.getAmount().toString() + " " + TEST_TRANSACTION2.getCurrency());
        assertThat(content).contains(TEST_TRANSACTION2.getDateTime().format(formatter)); // Используем formatter
    }


}
