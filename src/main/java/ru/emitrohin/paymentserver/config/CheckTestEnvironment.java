package ru.emitrohin.paymentserver.config;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckTestEnvironment {
    private final Environment environment;

    public boolean isTestEnvironment() {
        return !environment.matchesProfiles("prod");
    }
}
