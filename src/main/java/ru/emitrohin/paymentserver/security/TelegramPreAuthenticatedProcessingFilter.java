package ru.emitrohin.paymentserver.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class TelegramPreAuthenticatedProcessingFilter extends AbstractPreAuthenticatedProcessingFilter {

    //public static final String HEADER_NAME = "X-InitData";
    public static final String COOKIE_NAME = "initData";

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        return "Not handled here";
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return Optional.ofNullable(request.getCookies())
                .stream()
                .flatMap(Arrays::stream)
                .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}