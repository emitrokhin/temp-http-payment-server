package ru.emitrohin.paymentserver.dto.mapper;

import org.mapstruct.Mapper;
import ru.emitrohin.paymentserver.dto.CardResponse;
import ru.emitrohin.paymentserver.dto.cloudpayments.CloudpaymentsRequest;
import ru.emitrohin.paymentserver.model.Card;

@Mapper(componentModel = "spring")
public interface CardMapper {

    Card createFromRequest(CloudpaymentsRequest cloudpaymentsRequest);

    CardResponse toCardResponse(Card card);
}
