package ru.emitrohin.paymentserver.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import ru.emitrohin.paymentserver.security.*;

//TODO ChangeSessionIdAuthenticationStrategy : Changed session id from C5BF689A7EECF9FB0EE39C1623FB4D4B ??
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TelegramPreAuthenticatedAuthenticationProvider provider;

    private final CloudpaymentsSecurityFilter cloudPaymentsSecurityFilter;

    @Bean
    public FilterRegistrationBean<CloudpaymentsSecurityFilter> jwtAuthenticationFilterRegistration(CloudpaymentsSecurityFilter filter) {
        var registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false); //отключаем авто регистрацию контейнером.
        return registration;
    }


    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http, TelegramPreAuthenticatedProcessingFilter filter) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("frame-ancestors 'self' https://web.telegram.org")
                        )
                )
                .authenticationProvider(provider)
                .addFilterBefore(filter, AnonymousAuthenticationFilter.class)
                .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/auth",
                                "/error",
                                "/not-in-telegram",
                                "/unsupported",
                                "/static/**",
                                "/js/**",
                                "/css/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((request, response, authException) -> response.sendRedirect("/auth?redirect=" + request.getRequestURI()))
                );

        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain filterCloudpaymentsRequests(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                    .securityMatcher("/cloudpayments/**")
                    .addFilterBefore(cloudPaymentsSecurityFilter, AnonymousAuthenticationFilter.class)
                    .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(request ->
                            request.requestMatchers("/cloudpayments/**").permitAll()); //.authenticated() для такого метода надо дописать аутентификацию и создавать типа UserDetails
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(provider);
    }

    @Bean
    public TelegramPreAuthenticatedProcessingFilter telegramPreAuthenticatedProcessingFilter(AuthenticationManager authenticationManager) {
        TelegramPreAuthenticatedProcessingFilter filter = new TelegramPreAuthenticatedProcessingFilter();
        filter.setAuthenticationManager(authenticationManager); // Инжектируем AuthenticationManager через метод
        return filter;
    }
}