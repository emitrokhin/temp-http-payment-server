package ru.emitrohin.paymentserver.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class TelegramUserDetails implements UserDetails {

    private final long telegramId;

    public TelegramUserDetails(long telegramId) {
        this.telegramId = telegramId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // Если роли не используются
    }

    @Override
    public String getPassword() {
        return null; // Пароль не используется
    }

    @Override
    public String getUsername() {
        return String.valueOf(telegramId);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Логика проверки, если необходимо
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Логика проверки, если необходимо
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Логика проверки, если необходимо
    }

    @Override
    public boolean isEnabled() {
        return true; // Логика проверки, если необходимо
    }
}
