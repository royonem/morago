package com.roy.morago.mapper;

import com.roy.morago.dto.auth.ClientRegisterRequest;
import com.roy.morago.dto.auth.TranslatorRegisterRequest;
import com.roy.morago.dto.user.UpdateUserRequest;
import com.roy.morago.dto.user.UserResponse;
import com.roy.morago.entity.user.Language;
import com.roy.morago.entity.user.Role;
import com.roy.morago.entity.user.User;
import com.roy.morago.repository.user.LanguageRepository;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // CREATE
    User createUserFromDto(ClientRegisterRequest dto);
    User createUserFromDto(TranslatorRegisterRequest dto);
    // READ
    List<UserResponse> toUserResponse(List<User> users);

    @Mapping(target = "roles", source = "roles")
    @Mapping(target = "languages", source = "languages")
    UserResponse toUserResponse(User user);
    // UPDATE
    @Mapping(target = "languages", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(UpdateUserRequest dto, @MappingTarget User user);

    default Set<String> mapRoles(Set<Role> roles) {
        return roles == null ? Set.of() :
                roles.stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet());
    }

    default Set<String> mapLanguages(Set<Language> languages) {
        return languages == null ? Set.of() :
                languages.stream()
                        .map(Language::getName)
                        .collect(Collectors.toSet());
    }

}
