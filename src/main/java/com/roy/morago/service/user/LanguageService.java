package com.roy.morago.service.user;

import com.roy.morago.dto.user.LanguageRequest;
import com.roy.morago.dto.user.LanguageResponse;
import com.roy.morago.entity.user.Language;
import com.roy.morago.entity.user.User;
import com.roy.morago.mapper.LanguageMapper;
import com.roy.morago.repository.user.LanguageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class LanguageService {
    private final LanguageRepository languageRepository;
    private final LanguageMapper languageMapper;
    private final UserHelper helper;

    @Transactional
    public LanguageResponse createLanguage(LanguageRequest languageRequest) {
        log.info("Creating language: languageName={}", languageRequest.name());
        Language language = languageMapper.toEntity(languageRequest);
        Language savedLanguage = languageRepository.save(language);
        log.info("Language created: languageId={}, languageName={}",
                savedLanguage.getId(), savedLanguage.getName());
        return languageMapper.toResponse(savedLanguage);
    }

    public List<LanguageResponse> getAllLanguages() {
        List<Language> allLanguages = languageRepository.findAll();
        return languageMapper.toResponse(allLanguages);
    }

    @Transactional
    public void addLanguages(Long userId, List<Long> languageIds) {
        log.info("Adding languages: userId={}, languageIds={}", userId, languageIds);
        User user = helper.findUserById(userId);
        List<Language> languages = languageRepository.findAllById(languageIds);
        for (Language language : languages) {
            user.getLanguages().add(language);
            language.getUsers().add(user);
        }
        log.info("Languages added: userId={}, count={}", userId, languages.size());
    }

    @Transactional
    public void deleteLanguage(Long languageId) {
        log.info("Deleting language: languageId={}", languageId);
        Language language = helper.findLanguageById(languageId);
        languageRepository.delete(language);
        log.info("Language deleted: languageId={}, languageName={}", languageId, language.getName());
    }
}
