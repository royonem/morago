package com.roy.morago.mapper;

import com.roy.morago.dto.call.CallResponse;
import com.roy.morago.dto.call.CallRequest;
import com.roy.morago.entity.call.Call;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CallMapper {
    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "translatorId", source = "translator.id")
    @Mapping(target = "topicId", source = "topic.id")
    CallResponse toResponse(Call call);

    Call toEntity(CallRequest request);
}
