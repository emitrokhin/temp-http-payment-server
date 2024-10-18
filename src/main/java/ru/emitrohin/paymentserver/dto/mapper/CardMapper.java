package ru.emitrohin.paymentserver.dto.mapper;

import org.mapstruct.Mapper;
import ru.emitrohin.paymentserver.dto.CardResponse;
import ru.emitrohin.paymentserver.model.Card;

@Mapper(componentModel = "spring")
public interface CardMapper {

    CardResponse toCardResponse(Card card);
}
