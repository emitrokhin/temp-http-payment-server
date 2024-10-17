package ru.emitrohin.paymentserver.controller.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.emitrohin.paymentserver.dto.profile.ProfileUpdateDTO;
import ru.emitrohin.paymentserver.service.ProfileService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class ProfileRestController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileRestController.class);

    private final ProfileService profileService;

    @PostMapping("/update")
    public ResponseEntity<Object> getUser(@Valid @RequestBody ProfileUpdateDTO request, BindingResult bindingResult) {
        var telegramId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        if (bindingResult.hasErrors()) {
            logger.error("Binding result error: {}", bindingResult.getAllErrors() );
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        profileService.saveOrUpdateProfileUpdate(telegramId, request);
        return ResponseEntity.ok().build();
    }
}
