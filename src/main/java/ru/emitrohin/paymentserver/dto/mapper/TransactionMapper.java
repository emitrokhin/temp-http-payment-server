package ru.emitrohin.paymentserver.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.emitrohin.paymentserver.dto.TransactionResponse;
import ru.emitrohin.paymentserver.dto.cloudpayments.CloudpaymentsRequest;
import ru.emitrohin.paymentserver.model.Transaction;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "accountId", target = "telegramId")
    Transaction createFromRequest(CloudpaymentsRequest request);

    @Mapping(source = "accountId", target = "telegramId")
    void updateFromRequest(CloudpaymentsRequest request, @MappingTarget Transaction entity);

    @Mapping(source = "amount", target = "amount")
    @Mapping(source = "dateTime", target = "dateTime")
    @Mapping(source = "currency", target = "currency")
    TransactionResponse toTransactionResponse(Transaction transaction);
}
