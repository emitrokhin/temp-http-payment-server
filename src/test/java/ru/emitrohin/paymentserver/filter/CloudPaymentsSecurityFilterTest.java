package ru.emitrohin.paymentserver.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.emitrohin.paymentserver.config.PublicKeyProperty;
import ru.emitrohin.paymentserver.security.CloudpaymentsSecurityFilter;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CloudPaymentsSecurityFilterTest {

    //TODO перенести в другое место
    private final String VALID_IP = "162.55.174.97";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private PublicKeyProperty publicKeyProperty;

    @InjectMocks
    private CloudpaymentsSecurityFilter filter;

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldAllowRequestWithValidIpNoHeadersAndHmac() throws Exception {
        // Arrange: Мокаем корректные IP и HMAC
        when(request.getRemoteAddr()).thenReturn(VALID_IP);
        when(request.getHeader("X-Content-HMAC")).thenReturn("validHmac");
        when(request.getHeader("Content-HMAC")).thenReturn("validHmac");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(publicKeyProperty.publicKey()).thenReturn("validKey");

        // Act: Вызываем фильтр
        var spy = spy(filter);
        doReturn("validHmac").when(spy).calculateHMAC(anyString(), anyString());

        // Act: Вызываем фильтр
        spy.doFilter(request, response, filterChain);

        // Assert: Убедимся, что цепочка фильтров продолжается
        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).sendError(anyInt()); // Убедимся, что ошибки не отправлялись
    }

    @Test
    void shouldAllowRequestWithValidOnlyXForwardedAndHmac() throws Exception {
        // Arrange: Мокаем корректные IP и HMAC
        when(request.getHeader("X-Content-HMAC")).thenReturn("validHmac");
        when(request.getHeader("Content-HMAC")).thenReturn("validHmac");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(VALID_IP);
        when(publicKeyProperty.publicKey()).thenReturn("validKey");

        // Act: Вызываем фильтр
        var spy = spy(filter);
        doReturn("validHmac").when(spy).calculateHMAC(anyString(), anyString());

        // Act: Вызываем фильтр
        spy.doFilter(request, response, filterChain);

        // Assert: Убедимся, что цепочка фильтров продолжается
        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).sendError(anyInt()); // Убедимся, что ошибки не отправлялись
    }

    @Test
    void shouldAllowRequestWithValidOnlyXRealAndHmac() throws Exception {
        // Arrange: Мокаем корректные IP и HMAC
        when(request.getHeader("X-Content-HMAC")).thenReturn("validHmac");
        when(request.getHeader("Content-HMAC")).thenReturn("validHmac");
        when(request.getHeader("X-Forwarded-For")).thenReturn(VALID_IP);
        when(publicKeyProperty.publicKey()).thenReturn("validKey");

        // Act: Вызываем фильтр
        var spy = spy(filter);
        doReturn("validHmac").when(spy).calculateHMAC(anyString(), anyString());

        // Act: Вызываем фильтр
        spy.doFilter(request, response, filterChain);

        // Assert: Убедимся, что цепочка фильтров продолжается
        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).sendError(anyInt()); // Убедимся, что ошибки не отправлялись
    }


    @Test
    void shouldThrowAccessDeniedExceptionForInvalidIp() throws Exception {
        // Arrange: Мокаем недоверенный IP
        when(request.getRemoteAddr()).thenReturn("123.123.123.123");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);

        // Act: Вызываем фильтр
        filter.doFilter(request, response, filterChain);

        // Проверяем, что цепочка фильтров не продолжается
        verify(response, times(1)).sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: Invalid IP address");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldThrowAuthenticationExceptionForMissingHmacHeaders() throws Exception {
        // Arrange: Мокаем корректный IP, но отсутствующие HMAC заголовки
        when(request.getRemoteAddr()).thenReturn(VALID_IP);
        when(request.getHeader("X-Content-HMAC")).thenReturn(null);
        when(request.getHeader("Content-HMAC")).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);

        // Act: Вызываем фильтр
        filter.doFilter(request, response, filterChain);

        // Проверяем, что цепочка фильтров не продолжается
        verify(response, times(1)).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing HMAC headers");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    //TODO когда будет завершен рассчет HMAC поправить тест
    void shouldThrowAuthenticationExceptionForInvalidHmacSignature() throws Exception {
        // Arrange: Мокаем корректный IP и некорректную HMAC подпись
        when(request.getRemoteAddr()).thenReturn(VALID_IP);
        when(request.getHeader("X-Content-HMAC")).thenReturn("invalidHmac");
        when(request.getHeader("Content-HMAC")).thenReturn("invalidHmac");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(publicKeyProperty.publicKey()).thenReturn("validKey");

        // Act: Вызываем фильтр
        var spy = spy(filter);
        doReturn("validHmac").when(spy).calculateHMAC(anyString(), anyString());
        spy.doFilter(request, response, filterChain);

        // Проверяем, что цепочка фильтров не продолжается
        verify(response, times(1)).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid HMAC signature");
        verify(filterChain, never()).doFilter(request, response);
    }
}
