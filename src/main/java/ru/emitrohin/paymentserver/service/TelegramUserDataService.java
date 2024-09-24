package ru.emitrohin.paymentserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.emitrohin.paymentserver.dto.TelegramUserRequest;
import ru.emitrohin.paymentserver.dto.mapper.TelegramUserDataMapper;
import ru.emitrohin.paymentserver.model.TelegramUserData;
import ru.emitrohin.paymentserver.repository.TelegramUserDataRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TelegramUserDataService {

    private final TelegramUserDataRepository repository;

    private final TelegramUserDataMapper userMapper;

    public Optional<TelegramUserData> findByTelegramId(long telegramId) {
        return repository.findByTelegramId(telegramId);
    }

    public void saveOrUpdate(TelegramUserRequest telegramUserRequest) {
        var telegramUserData = repository.findByTelegramId(telegramUserRequest.telegramId())
                .orElseGet(TelegramUserData::new);
        userMapper.updateFromRequest(telegramUserRequest, telegramUserData);
        repository.save(telegramUserData);
    }
}