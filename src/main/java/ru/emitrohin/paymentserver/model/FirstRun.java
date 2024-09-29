package ru.emitrohin.paymentserver.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class FirstRun extends BaseEntity {

    @Column(name = "telegram_id", nullable = false, unique = true)
    private Long telegramId;
}
