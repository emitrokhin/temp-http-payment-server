package ru.emitrohin.paymentserver.config;

import ch.qos.logback.classic.LoggerContext;
import com.bugsnag.BugsnagAppender;
import com.bugsnag.Report;
import jakarta.annotation.PostConstruct;

import ch.qos.logback.classic.Logger;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BugsnagCallbackConfiguration {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(BugsnagCallbackConfiguration.class);

    @PostConstruct
    public void addUserCallbackToBugsnag() {
        var loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        var rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

        var appender = rootLogger.getAppender("BUGSNAG");
        if (appender instanceof BugsnagAppender bugsnagAppender) {
            var client = bugsnagAppender.getClient();
            client.addCallback((Report report) -> {
                var userId = MDC.get("userId");
                report.setUser(userId, null, null);
            });
        } else {
            logger.warn("BugsnagAppender not found in root logger. Bugsnag works only in production environment.");
        }
    }
}