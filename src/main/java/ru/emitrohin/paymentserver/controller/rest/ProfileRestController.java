package ru.emitrohin.paymentserver.controller.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.emitrohin.paymentserver.dto.profile.ProfilePaymentDTO;
import ru.emitrohin.paymentserver.dto.profile.ProfileUpdateDTO;
import ru.emitrohin.paymentserver.service.ProfileService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class ProfileRestController {

    private final ProfileService profileService;

    @PostMapping("/update")
    public ResponseEntity<Object> getUser(@Valid @RequestBody ProfileUpdateDTO request, BindingResult bindingResult) {
        var telegramId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        profileService.saveOrUpdateProfileUpdate(telegramId, request);
        return ResponseEntity.ok().build();
    }
}
