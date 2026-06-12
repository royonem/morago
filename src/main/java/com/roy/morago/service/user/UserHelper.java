package com.roy.morago.service.user;

import com.roy.morago.entity.user.Language;
import com.roy.morago.entity.user.User;
import com.roy.morago.exception.user.LanguageNotFoundException;
import com.roy.morago.exception.user.UserNotFoundException;
import com.roy.morago.repository.user.LanguageRepository;
import com.roy.morago.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserHelper {
    UserRepository userRepository;
    LanguageRepository languageRepository;

    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public User findUserWithAuthentication(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    protected Language findLanguageById(Long languageId) {
        return languageRepository.findById(languageId)
                .orElseThrow(() -> new LanguageNotFoundException("Language not found"));
    }
}
