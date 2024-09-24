package ru.emitrohin.paymentserver.util;

import jakarta.servlet.http.HttpServletRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpUtil {

    public static String getClientIp(HttpServletRequest request) {
        var ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // X-Forwarded-For может содержать список IP-адресов, если запрос прошел через несколько прокси,
        // в таком случае реальный IP-адрес клиента будет первым в списке
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAddress;
    }

    public static boolean isIpInRange(String ipAddress, String cidr) throws UnknownHostException {
        // Если маска не указана, добавляем /32, что означает одиночный IP-адрес
        if (!cidr.contains("/")) {
            cidr = cidr + "/32";
        }

        var parts = cidr.split("/");
        var ipInRange = parts[0];
        var prefixLength = Integer.parseInt(parts[1]);

        var targetAddress = InetAddress.getByName(ipAddress);
        var rangeAddress = InetAddress.getByName(ipInRange);

        var targetBytes = targetAddress.getAddress();
        var rangeBytes = rangeAddress.getAddress();

        var byteCount = prefixLength / 8;
        var bitCount = prefixLength % 8;

        // Побайтовая проверка
        for (var i = 0; i < byteCount; i++) {
            if (targetBytes[i] != rangeBytes[i]) {
                return false;
            }
        }

        // Проверка битов в последнем байте
        if (bitCount > 0) {
            var mask = 0xFF << (8 - bitCount);
            return (targetBytes[byteCount] & mask) == (rangeBytes[byteCount] & mask);
        }

        return true;
    }
}
