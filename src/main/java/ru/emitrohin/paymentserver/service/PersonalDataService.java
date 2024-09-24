package ru.emitrohin.paymentserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.emitrohin.paymentserver.dto.PersonalDataPaymentRequest;
import ru.emitrohin.paymentserver.dto.mapper.PersonalDataMapper;
import ru.emitrohin.paymentserver.model.PersonalData;
import ru.emitrohin.paymentserver.repository.PersonalDataRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PersonalDataService {

    private final PersonalDataRepository repository;

    private final PersonalDataMapper mapper;

    public Optional<PersonalData> findByTelegramId(long telegramId) {
        return repository.findByTelegramId(telegramId);
    }

    public void saveOrUpdate(long telegramId, PersonalDataPaymentRequest request) {
        var telegramUserData = repository.findByTelegramId(telegramId)
                .orElseGet(PersonalData::new);
        mapper.updateFromRequest(request, telegramUserData);
        repository.save(telegramUserData);
    }
}
