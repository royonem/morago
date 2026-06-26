package com.roy.morago.mapper;

import com.roy.morago.dto.topic.TopicRequest;
import com.roy.morago.dto.topic.TopicResponse;
import com.roy.morago.entity.topic.Topic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TopicMapper {
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "iconId", source = "icon.id")
    TopicResponse toResponse(Topic topic);
    Topic toEntity(TopicRequest request);
}