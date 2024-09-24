package ru.emitrohin.paymentserver.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.emitrohin.paymentserver.service.TelegramAuthenticationService;

@Component
public class TelegramPreAuthenticatedAuthenticationProvider implements AuthenticationProvider {

    private final TelegramAuthenticationService telegramAuthService;

    public TelegramPreAuthenticatedAuthenticationProvider(TelegramAuthenticationService telegramAuthService) {
        this.telegramAuthService = telegramAuthService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var initData = (String) authentication.getCredentials();

        try {
            var userDetails = telegramAuthService.authenticateTelegramUser(initData);
            return new PreAuthenticatedAuthenticationToken(userDetails, initData, userDetails.getAuthorities());
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid initData", e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication);
    }
}