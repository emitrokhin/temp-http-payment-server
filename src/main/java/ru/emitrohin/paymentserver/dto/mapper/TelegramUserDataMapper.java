package ru.emitrohin.paymentserver.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.emitrohin.paymentserver.dto.TelegramUserRequest;
import ru.emitrohin.paymentserver.model.TelegramUserData;

@Mapper(componentModel = "spring")
public interface TelegramUserDataMapper {
    
    TelegramUserData createFromRequest(TelegramUserRequest request);
    void updateFromRequest(TelegramUserRequest request, @MappingTarget TelegramUserData telegramUserData);
}
