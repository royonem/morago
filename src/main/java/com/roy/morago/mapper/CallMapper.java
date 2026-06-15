package com.roy.morago.mapper;

import com.roy.morago.dto.call.CallResponse;
import com.roy.morago.dto.call.CallRequest;
import com.roy.morago.entity.call.Call;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CallMapper {
    CallResponse createResponseFromEntity(Call call);

    Call createEntityFromRequest(CallRequest callRequest);
}
