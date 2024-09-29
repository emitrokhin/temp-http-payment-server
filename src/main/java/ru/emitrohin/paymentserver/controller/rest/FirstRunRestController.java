package ru.emitrohin.paymentserver.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.emitrohin.paymentserver.service.FirstRunService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/first-run")
public class FirstRunRestController {
    private final FirstRunService firstRunService;

    @PostMapping("/complete")
    public ResponseEntity<Object> complete() {
        var telegramId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        if (firstRunService.findFirstRun(telegramId).isEmpty()) {
            firstRunService.completeFirstRun(telegramId);
        }

        return ResponseEntity.ok().build();
    }
}
