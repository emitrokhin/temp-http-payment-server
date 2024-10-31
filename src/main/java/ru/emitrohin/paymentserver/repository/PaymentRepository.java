package ru.emitrohin.paymentserver.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.emitrohin.paymentserver.model.Payment;
import ru.emitrohin.paymentserver.model.PaymentStatus;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findById(UUID id);
    Optional<Payment> findByTelegramIdAndPaymentStatus(long telegramId, PaymentStatus paymentStatus);
}
