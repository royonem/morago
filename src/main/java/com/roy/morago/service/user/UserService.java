package com.roy.morago.service.user;

import com.roy.morago.dto.auth.ClientRegisterRequest;
import com.roy.morago.dto.auth.TranslatorRegisterRequest;
import com.roy.morago.dto.user.UpdateUserRequest;
import com.roy.morago.dto.user.UserResponse;
import com.roy.morago.entity.user.Language;
import com.roy.morago.entity.user.User;
import com.roy.morago.mapper.UserMapper;
import com.roy.morago.repository.user.LanguageRepository;
import com.roy.morago.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;
    private final UserMapper userMapper;

    public void createUser(ClientRegisterRequest userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        User user = userMapper.createUserFromDto(userDto);
        userRepository.save(user);
    }

    public void createUser(TranslatorRegisterRequest userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        User user = userMapper.createUserFromDto(userDto);
        userRepository.save(user);
    }

    @Transactional
    public void updateUser(Long id, UpdateUserRequest userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userMapper.updateUserFromDto(userDto, user);

        if (userDto.getLanguages() != null) {
            Set<Language> languages = languageRepository.findAllByNameIn(userDto.getLanguages());
            user.setLanguages(languages);
        }
        userRepository.save(user);
    }

    public List<UserResponse> getAllUsers() {
        return userMapper.toUserResponse(userRepository.findAll());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toUserResponse(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
