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
import ru.emitrohin.paymentserver.dto.profile.ProfileUpdateDTO;
import ru.emitrohin.paymentserver.model.Profile;
import ru.emitrohin.paymentserver.service.FirstRunService;
import ru.emitrohin.paymentserver.service.ProfileService;
import ru.emitrohin.paymentserver.dto.mapper.ProfileMapper;

import java.time.LocalDate;
import java.util.Optional;

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

    @Test
    @WithMockUser(username = "1234567890")
    void getUser_ProfileExists_ShouldReturnProfilePage() throws Exception {
        // Мокируем SecurityContext
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("1234567890"); // Пример Telegram ID
        SecurityContextHolder.setContext(securityContext);

        // Создаем тестовый профиль
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

        // Мокируем поведение сервисов
        when(profileService.findByTelegramId(1234567890L)).thenReturn(Optional.of(profile));
        when(firstRunService.findFirstRun(1234567890L)).thenReturn(Optional.empty());
        when(profileMapper.createUpdateResponse(profile)).thenReturn(profileDTO);

        // Выполняем GET-запрос и проверяем результат
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("profileForm"))
                .andExpect(model().attributeExists("firstRun"))
                .andExpect(view().name("profile"));
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
