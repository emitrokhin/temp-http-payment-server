package ru.emitrohin.paymentserver.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.emitrohin.paymentserver.model.Payment;
import ru.emitrohin.paymentserver.model.PaymentStatus;
import ru.emitrohin.paymentserver.repository.PaymentRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    private static final Long TELEGRAM_ID = 123456789L;

    private static final UUID PAYMENT_ID = UUID.randomUUID();
    private static final Payment PENDING_PAYMENT = createPayment(PAYMENT_ID, PaymentStatus.PENDING);
    private static final Payment COMPLETED_PAYMENT = createPayment(PAYMENT_ID, PaymentStatus.SUCCESS);

    private static Payment createPayment(UUID id, PaymentStatus paymentStatus) {
        var payment = new Payment();
        payment.setId(id);
        payment.setTelegramId(TELEGRAM_ID);
        payment.setPaymentStatus(paymentStatus);
        return payment;
    }

    @Test
    void testSave() {
        paymentService.save(PENDING_PAYMENT);
        verify(paymentRepository, times(1)).save(PENDING_PAYMENT);
    }

    @Test
    void testCreatePayment() {
        when(paymentRepository.save(any(Payment.class))).thenReturn(PENDING_PAYMENT);
        var createdPayment = paymentService.createPayment(TELEGRAM_ID);
        assertEquals(PaymentStatus.PENDING, createdPayment.getPaymentStatus());
        assertEquals(TELEGRAM_ID, createdPayment.getTelegramId());
        verify(paymentRepository, times(1)).save(createdPayment);
    }

    @Test
    void testUpdatePaymentStatus() {
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(PENDING_PAYMENT));
        paymentService.updatePaymentStatus(PAYMENT_ID, PaymentStatus.SUCCESS);
        assertEquals(PaymentStatus.SUCCESS, PENDING_PAYMENT.getPaymentStatus());
        verify(paymentRepository, times(1)).findById(PAYMENT_ID);
        verify(paymentRepository, times(1)).save(PENDING_PAYMENT);
    }

    @Test
    void testGetLastPendingPayment() {
        when(paymentRepository.findByTelegramIdAndPaymentStatus(TELEGRAM_ID, PaymentStatus.PENDING)).thenReturn(Optional.of(PENDING_PAYMENT));
        var result = paymentService.getLastPendingPayment(TELEGRAM_ID);
        assertTrue(result.isPresent());
        assertEquals(PENDING_PAYMENT, result.get());
        verify(paymentRepository, times(1)).findByTelegramIdAndPaymentStatus(TELEGRAM_ID, PaymentStatus.PENDING);
    }
}
