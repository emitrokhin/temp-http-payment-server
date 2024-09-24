package ru.emitrohin.paymentserver.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Entity
@Table(name = "all_incoming_users")
@Getter
@Setter
public class TelegramUserData implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private Long telegramId;

    @NotNull(message = "Имя не должно быть пустым")
    private String firstName;

    @Size(max = 100, message = "Фамилия не должна превышать 100 символов")
    private String lastName;

    @Size(max = 100, message = "Фамилия не должна превышать 100 символов")
    private String username;

    @Size(max = 10, message = "Код языка не должен превышать 10 символов")
    private String languageCode;

    private Boolean allowsWriteToPm;

    @Size(max = 255, message = "URL фотографии не должен превышать 255 символов")
    private String photoUrl;

    @Column(nullable = false)
    @CreationTimestamp
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @Column(nullable = false)
    @UpdateTimestamp
    private ZonedDateTime updatedAt = ZonedDateTime.now();
}
