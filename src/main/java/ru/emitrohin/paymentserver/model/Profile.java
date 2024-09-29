package ru.emitrohin.paymentserver.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "profiles")
public class Profile extends BaseEntity {

    @Column(nullable = false, unique = true)
    private Long telegramId;

    @Size(min = 1, max = 100, message = "Имя должно содержать от 1 до 100 символов")
    @Column(nullable = false)
    private String firstName;

    @Size(min = 1, max = 100, message = "Фамилия должна содержать от 1 до 100 символов")
    @Column(nullable = false)
    private String lastName;

    @Size(min = 10, max = 15, message = "Телефон должен содержать от 10 до 15 символов")
    @Column(nullable = false)
    private String phone;

    @Email(message = "Неверный формат email")
    @Column(nullable = false)
    private String email;

    @PastOrPresent(message = "Дата не может быть будущим")
    @Column
    private LocalDate dateOfBirth;

    @Column
    private String city;

    @Column
    private String profession;
}
