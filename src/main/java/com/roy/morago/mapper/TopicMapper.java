package com.roy.morago.mapper;

import com.roy.morago.dto.topic.TopicRequest;
import com.roy.morago.dto.topic.TopicResponse;
import com.roy.morago.entity.topic.Topic;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TopicMapper {
    TopicResponse createResponseFromEntity(Topic topic);
    Topic createEntityFromRequest(TopicRequest request);
}
