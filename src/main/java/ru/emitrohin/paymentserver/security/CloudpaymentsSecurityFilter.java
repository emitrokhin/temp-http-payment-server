package ru.emitrohin.paymentserver.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.emitrohin.paymentserver.config.CloudpaymentsProperties;
import ru.emitrohin.paymentserver.util.IpUtil;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;

@EnableConfigurationProperties(CloudpaymentsProperties.class)
@RequiredArgsConstructor
@Configuration
public class CloudpaymentsSecurityFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(CloudpaymentsSecurityFilter.class);

    private static final Set<String> TRUSTED_SUBNETS = Set.of(
            "91.142.84.0/27", "87.251.91.160/27", "162.55.174.97/32",
            "194.39.64.130/32", "92.63.206.131/32", "185.98.81.0/28",
            "46.46.175.96/27", "46.46.168.160/27"
    );

    private final CloudpaymentsProperties publicKeyProperty;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. Проверка IP-адреса
        var ipAddress = IpUtil.getClientIp(request);
        var validIp = TRUSTED_SUBNETS.stream()
                .anyMatch(subnet -> {
                    try {
                        return IpUtil.isIpInRange(ipAddress, subnet);
                    } catch (UnknownHostException e) {
                        logger.error("Access denied. Unknown IP address: {}", ipAddress, e);
                        return false;
                    }
                });

        if (!validIp) {
            logger.error("Access denied. Invalid IP address: {}", ipAddress);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Invalid IP address");
            return;
        }

        // 2. Проверка заголовков HMAC
        var xContentHmac = request.getHeader("X-Content-HMAC");
        var contentHmac = request.getHeader("Content-HMAC");

        if (xContentHmac == null || contentHmac == null) {
            logger.error("Access denied. Missing HMAC headers");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing HMAC headers");
            return;
        }

        // 3. Проверка HMAC подписи
        var requestBody = new StringBuilder();
        request.getParameterMap()
                .forEach((key, value) -> requestBody.append(key).append("=").append(String.join(",", value)).append("&"));

        // Удаляем последний "&"
        if (!requestBody.isEmpty()) {
            requestBody.setLength(requestBody.length() - 1);
        }

        var calculatedHmac = calculateHMAC(requestBody.toString(), publicKeyProperty.publicKey());

        // TODO: проверить HMAC подпись
        // if (!xContentHmac.equals(calculatedHmac)) {
        //     logger.error("Invalid HMAC signature");
        //     response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid HMAC signature");
        //     return;
        // }

        filterChain.doFilter(request, response);
    }

    public String calculateHMAC(String data, String key) throws ServletException {
        try {
            var mac = Mac.getInstance("HmacSHA256");
            var secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            var hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return new String(Base64.getEncoder().encode(hmacBytes), StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("Failed to calculate HMAC. Reason: {}", e.getMessage());
            throw new ServletException("Failed to calculate HMAC", e);
        }
    }
}