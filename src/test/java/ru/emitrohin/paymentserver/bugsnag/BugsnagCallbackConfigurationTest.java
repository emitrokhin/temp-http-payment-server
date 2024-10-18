package ru.emitrohin.paymentserver.bugsnag;

import ch.qos.logback.classic.LoggerContext;
import com.bugsnag.Bugsnag;
import com.bugsnag.BugsnagAppender;
import com.bugsnag.Report;
import com.bugsnag.callbacks.Callback;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import ru.emitrohin.paymentserver.config.BugsnagCallbackConfiguration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class BugsnagCallbackConfigurationTest {

    @InjectMocks
    private BugsnagCallbackConfiguration bugsnagCallbackConfiguration;

    @Mock
    private BugsnagAppender bugsnagAppenderMock;

    @Mock
    private Bugsnag bugsnagClientMock;

    @Mock
    private LoggerContext loggerContextMock;

    @Mock
    private ch.qos.logback.classic.Logger rootLoggerMock;

    @Test
    public void testAddUserCallbackToBugsnag() {
        // Настраиваем мок LoggerContext, чтобы возвращать rootLogger
        when(bugsnagAppenderMock.getClient()).thenReturn(bugsnagClientMock);
        when(loggerContextMock.getLogger(any(String.class))).thenReturn(rootLoggerMock);

        // Настраиваем rootLogger, чтобы возвращать BugsnagAppender
        when(rootLoggerMock.getAppender("BUGSNAG")).thenReturn(bugsnagAppenderMock);

        // Выполняем метод addUserCallbackToBugsnag
        bugsnagCallbackConfiguration.addUserCallbackToBugsnag();

        // Захватываем callback, который был добавлен в Bugsnag
        var callbackCaptor = ArgumentCaptor.forClass(Callback.class);
        verify(bugsnagClientMock).addCallback(callbackCaptor.capture());

        // Проверяем, что callback использует MDC для установки userId
        var callback = callbackCaptor.getValue();
        var reportMock = mock(Report.class);

        // Помещаем значение в MDC
        MDC.put("userId", "testUserId");

        // Выполняем callback с захваченным Report
        callback.beforeNotify(reportMock);

        // Проверяем, что в report был установлен правильный userId
        verify(reportMock).setUser("testUserId", null, null);

        // Убеждаемся, что MDC был вызван корректно
        assertThat(MDC.get("userId")).isEqualTo("testUserId");
    }
}
