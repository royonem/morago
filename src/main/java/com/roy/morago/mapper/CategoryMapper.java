package com.roy.morago.mapper;

import com.roy.morago.dto.topic.CategoryResponse;
import com.roy.morago.entity.topic.Category;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {
    CategoryResponse createResponseFromEntity(Category category);
}
