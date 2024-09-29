package ru.emitrohin.paymentserver.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.emitrohin.paymentserver.dto.profile.ProfilePaymentDTO;
import ru.emitrohin.paymentserver.dto.profile.ProfileUpdateDTO;
import ru.emitrohin.paymentserver.model.Profile;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    ProfileUpdateDTO createUpdateResponse(Profile profile);
    ProfilePaymentDTO createPaymentResponse(Profile profile);
    void updateFromPaymentRequest(ProfilePaymentDTO request, @MappingTarget Profile entity);
    void updateFromUpdateRequest(ProfileUpdateDTO request, @MappingTarget Profile entity);
}
