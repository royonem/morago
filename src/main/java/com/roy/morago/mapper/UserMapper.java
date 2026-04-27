package com.roy.morago.mapper;

import com.roy.morago.dto.auth.ClientRegisterRequest;
import com.roy.morago.dto.auth.TranslatorRegisterRequest;
import com.roy.morago.dto.user.UpdateUserRequest;
import com.roy.morago.dto.user.UserResponse;
import com.roy.morago.entity.user.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // CREATE
    User createUserFromDto(ClientRegisterRequest dto);
    User createUserFromDto(TranslatorRegisterRequest dto);
    // READ
    List<UserResponse> toUserResponse(List<User> users);
    UserResponse toUserResponse(User user);
    // UPDATE
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(UpdateUserRequest dto, @MappingTarget User user);

}
