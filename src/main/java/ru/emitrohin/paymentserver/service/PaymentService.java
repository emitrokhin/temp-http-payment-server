package ru.emitrohin.paymentserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.emitrohin.paymentserver.model.Payment;
import ru.emitrohin.paymentserver.model.PaymentStatus;
import ru.emitrohin.paymentserver.repository.PaymentRepository;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public void save(Payment payment) {
        paymentRepository.save(payment);
    }

    public Payment createPayment(long telegramId) {
        var payment = new Payment();
        payment.setTelegramId(telegramId);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        save(payment);
        return payment;
    }

    public void updatePaymentStatus(UUID paymentId, PaymentStatus paymentStatus) {
        var payment = paymentRepository.findById(paymentId);
        if (payment.isPresent()) {
            var updatedPayment = payment.get();
            updatedPayment.setPaymentStatus(paymentStatus);
            paymentRepository.save(updatedPayment);
        }
    }

    public Optional<Payment> getLastPendingPayment(long telegramId) {
        return paymentRepository.findByTelegramIdAndPaymentStatus(telegramId, PaymentStatus.PENDING);
    }
}