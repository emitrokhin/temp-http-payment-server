package ru.emitrohin.paymentserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.emitrohin.paymentserver.model.PersonalData;
import ru.emitrohin.paymentserver.service.PersonalDataService;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final PersonalDataService personalDataService;

    @GetMapping("/profile")
    public String getUser(Model model) {
        var personalData = personalDataService.findByTelegramId(12345L); // пример получения данных
        model.addAttribute("personalData", personalData);
        return "profile";
    }

    @PostMapping("/updatePersonalData")
    public String updatePersonalData(@ModelAttribute PersonalData personalData, Model model) {
        // Логика обновления данных пользователя
        //repository.update(personalData);

        // Добавляем обновленные данные в модель и возвращаем нужный шаблон
        model.addAttribute("personalData", personalData);
        return "redirect:/profile"; // перенаправление на страницу после успешного сохранения
    }
}
