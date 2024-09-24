package ru.emitrohin.paymentserver.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "first_run")
@Getter
@Setter
public class FirstRun {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(nullable = false)
    private Long id;

    @Column(name = "telegram_id", nullable = false, unique = true)
    private Long telegramId;
}
