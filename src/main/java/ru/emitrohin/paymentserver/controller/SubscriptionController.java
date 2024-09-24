package ru.emitrohin.paymentserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.emitrohin.paymentserver.model.Subscription;
import ru.emitrohin.paymentserver.service.SubscriptionService;

//TODO проверить, такое ощущение, что направильные данные дает
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService service;

    @GetMapping("/subscription")
    public ResponseEntity<Subscription> getCurrentSubscription() {
        var telegramId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        return service.findCurrentSubscription(telegramId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.ACCEPTED).build());
    }
}