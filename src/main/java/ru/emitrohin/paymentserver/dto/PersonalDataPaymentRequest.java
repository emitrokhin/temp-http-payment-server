package ru.emitrohin.paymentserver.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersonalDataPaymentRequest {

    @NotNull(message = "Имя не должно быть пустым")
    @Size(min = 2, max = 100, message = "Имя должно содержать от 1 до 100 символов")
    private String firstName;

    @NotNull(message = "Фамилия не должна быть пустой")
    @Size(min = 2, max = 100, message = "Фамилия должна содержать от 1 до 100 символов")
    private String lastName;

    @NotNull(message = "Телефон не должен быть пустым")
    @Size(min = 11, max = 12, message = "Телефон должен содержать от 11 до 12 символов")
    private String phone;

    @NotNull(message = "Email не должен быть пустым")
    @Email(message = "Неверный формат email")
    private String email;
}
