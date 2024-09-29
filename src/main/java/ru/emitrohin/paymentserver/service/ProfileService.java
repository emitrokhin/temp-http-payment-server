package ru.emitrohin.paymentserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.emitrohin.paymentserver.dto.profile.ProfilePaymentDTO;
import ru.emitrohin.paymentserver.dto.mapper.ProfileMapper;
import ru.emitrohin.paymentserver.dto.profile.ProfileUpdateDTO;
import ru.emitrohin.paymentserver.model.Profile;
import ru.emitrohin.paymentserver.repository.PersonalDataRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final PersonalDataRepository repository;

    private final ProfileMapper mapper;

    public Optional<Profile> findByTelegramId(long telegramId) {
        return repository.findByTelegramId(telegramId);
    }

    public void saveOrUpdateProfilePayment(long telegramId, ProfilePaymentDTO request) {
        var telegramUserData = repository.findByTelegramId(telegramId)
                .orElseGet(Profile::new);
        telegramUserData.setTelegramId(telegramId); //TODO в маппер надо?
        mapper.updateFromPaymentRequest(request, telegramUserData);
        repository.save(telegramUserData);
    }

    public void saveOrUpdateProfileUpdate(long telegramId, ProfileUpdateDTO request) {
        var telegramUserData = repository.findByTelegramId(telegramId)
                .orElseGet(Profile::new);
        telegramUserData.setTelegramId(telegramId); //TODO в маппер надо?
        mapper.updateFromUpdateRequest(request, telegramUserData);
        repository.save(telegramUserData);
    }
}
