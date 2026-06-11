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
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;
    private final UserMapper userMapper;
    private final UserHelper helper;

    public UserResponse getUser(Long userId) {
        return userMapper.toUserResponse(helper.findUserById(userId));
    }

    public List<UserResponse> getAllUsers() {
        return userMapper.toUserResponse(userRepository.findAll());
    }

    @Transactional
    public void updateUser(Long id, UpdateUserRequest updateUserRequest) {
        User user = helper.findUserById(id);

        if (updateUserRequest.languages() != null) {
            Set<Language> languages = languageRepository.findAllByNameIn(updateUserRequest.languages());
            user.setLanguages(languages);
        }
        userMapper.updateUserFromDto(updateUserRequest, user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = helper.findUserById(id);
        userRepository.delete(user);
    }

}
