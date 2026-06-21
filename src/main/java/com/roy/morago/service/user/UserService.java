package com.roy.morago.service.user;

import com.roy.morago.dto.user.UpdateUserRequest;
import com.roy.morago.dto.user.UserResponse;
import com.roy.morago.dto.user.UserSearchRequest;
import com.roy.morago.entity.user.Language;
import com.roy.morago.entity.user.User;
import com.roy.morago.mapper.UserMapper;
import com.roy.morago.repository.user.LanguageRepository;
import com.roy.morago.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Set;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;
    private final UserMapper userMapper;
    private final UserHelper helper;

    public UserResponse getUser(Long userId) {
        return userMapper.createResponseFromEntity(helper.findUserById(userId));
    }

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::createResponseFromEntity);
    }

    public Page<UserResponse> searchUsers(UserSearchRequest request) {
        Specification<User> spec = helper.buildSpecification(request);
        return userRepository.findAll(spec, request.toPageable())
                .map(userMapper::createResponseFromEntity);
    }

    @Transactional
    public void updateUser(Long id, UpdateUserRequest updateUserRequest) {
        User user = helper.findUserById(id);

        if (updateUserRequest.languages() != null) {
            Set<Language> languages = languageRepository.findAllByNameIn(updateUserRequest.languages());
            user.setLanguages(languages);
        }
        userMapper.updateEntityFromRequest(updateUserRequest, user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = helper.findUserById(id);
        userRepository.delete(user);
    }

}
