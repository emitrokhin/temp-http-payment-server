package ru.emitrohin.paymentserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.emitrohin.paymentserver.dto.mapper.ProfileMapper;
import ru.emitrohin.paymentserver.service.FirstRunService;
import ru.emitrohin.paymentserver.service.ProfileService;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    private final FirstRunService firstRunService;

    private final ProfileMapper profileMapper;

    @GetMapping("/profile")
    public String getUser(Model model) {
        var telegramId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        //TODO что делать, если не найдено? На платеж вести?
        var profileForm = profileService.findByTelegramId(telegramId);
        if (profileForm.isEmpty()) {
            return "redirect:/";
        }

        var firstRunEntry = firstRunService.findFirstRun(telegramId);
        model.addAttribute("firstRun", firstRunEntry.isEmpty());
        model.addAttribute("profileForm", profileMapper.createUpdateResponse(profileForm.get()));
        return "profile";
    }
}
