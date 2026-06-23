package com.roy.morago.mapper;

import com.roy.morago.dto.user.LanguageRequest;
import com.roy.morago.dto.user.LanguageResponse;
import com.roy.morago.entity.user.Language;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LanguageMapper {
    LanguageResponse toResponse(Language language);
    List<LanguageResponse> toResponse(List<Language> languages);
    Language toEntity(LanguageRequest request);
}
