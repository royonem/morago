package com.roy.morago.service.user;

import com.roy.morago.dto.user.LanguageRequest;
import com.roy.morago.dto.user.LanguageResponse;
import com.roy.morago.entity.user.Language;
import com.roy.morago.entity.user.User;
import com.roy.morago.mapper.LanguageMapper;
import com.roy.morago.repository.user.LanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class LanguageService {
    private final LanguageRepository languageRepository;
    private final LanguageMapper languageMapper;
    private final UserHelper helper;

    @Transactional
    public LanguageResponse createLanguage(LanguageRequest languageRequest) {
        Language language = languageMapper.toEntity(languageRequest);
        return languageMapper.toResponse(languageRepository.save(language));
    }

    public List<LanguageResponse> getAllLanguages() {
        List<Language> allLanguages = languageRepository.findAll();
        return languageMapper.toResponse(allLanguages);
    }

    @Transactional
    public void addLanguages(Long userId, List<Long> languageIds) {
        User user = helper.findUserById(userId);
        List<Language> languages = languageRepository.findAllById(languageIds);
        for (Language language : languages) {
            user.getLanguages().add(language);
            language.getUsers().add(user);
        }
    }

    @Transactional
    public void deleteLanguage(Long languageId) {
        Language language = helper.findLanguageById(languageId);
        languageRepository.delete(language);
    }
}
