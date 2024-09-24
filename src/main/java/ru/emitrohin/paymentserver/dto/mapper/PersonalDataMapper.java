package ru.emitrohin.paymentserver.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.emitrohin.paymentserver.dto.PersonalDataPaymentRequest;
import ru.emitrohin.paymentserver.dto.PersonalDataPaymentResponse;
import ru.emitrohin.paymentserver.model.PersonalData;

@Mapper(componentModel = "spring")
public interface PersonalDataMapper {

    PersonalData createFromRequest(PersonalDataPaymentRequest request);
    PersonalDataPaymentResponse createResponse(PersonalData request);
    void updateFromRequest(PersonalDataPaymentRequest request, @MappingTarget PersonalData entity);
}
