package ru.emitrohin.paymentserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.emitrohin.paymentserver.model.FirstRun;
import ru.emitrohin.paymentserver.repository.FirstRunRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FirstRunService {

    private final FirstRunRepository repository;

    public Optional<FirstRun> findFirstRun(long telegramId) {
        return repository.findByTelegramId(telegramId);
    }

    public void completeFirstRun(long telegramId) {
        var firstRun = new FirstRun();
        firstRun.setTelegramId(telegramId);
        repository.save(firstRun);
    }
}
