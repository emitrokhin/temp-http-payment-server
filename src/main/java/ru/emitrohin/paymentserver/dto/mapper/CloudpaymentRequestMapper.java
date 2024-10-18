package ru.emitrohin.paymentserver.dto.mapper;

import org.mapstruct.Mapper;
import ru.emitrohin.paymentserver.dto.cloudpayments.CloudpaymentsRequest;

@Mapper(componentModel = "spring")
public interface CloudpaymentRequestMapper {

    CloudpaymentsRequest toCloudpaymentsRequest(CloudpaymentsRequest request);
}
