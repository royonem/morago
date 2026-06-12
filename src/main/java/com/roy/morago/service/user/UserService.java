package com.roy.morago.service.user;

import com.roy.morago.dto.user.UpdateUserRequest;
import com.roy.morago.dto.user.UserResponse;
import com.roy.morago.entity.user.Language;
import com.roy.morago.entity.user.User;
import com.roy.morago.exception.user.UserNotFoundException;
import com.roy.morago.mapper.UserMapper;
import com.roy.morago.repository.user.LanguageRepository;
import com.roy.morago.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;
    private final UserMapper userMapper;

    public User findUserWithAuthentication(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public UserResponse getUserById(Long userId) {
        return userMapper.toUserResponse(findUserById(userId));
    }

    public List<UserResponse> getAllUsers() {
        return userMapper.toUserResponse(userRepository.findAll());
    }

    @Transactional
    public void updateUser(Long id, UpdateUserRequest userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        userMapper.updateUserFromDto(userDto, user);

        if (userDto.getLanguages() != null) {
            Set<Language> languages = languageRepository.findAllByNameIn(userDto.getLanguages());
            user.setLanguages(languages);
        }
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new UserNotFoundException("User not found: " + id);
        }
    }

    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
