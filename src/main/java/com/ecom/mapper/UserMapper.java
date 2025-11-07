package com.ecom.mapper;

import com.ecom.dto.auth.AuthResponse;
import com.ecom.dto.common.UserDto;
import com.ecom.entity.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserDto toUserDto(User user) {
        if (user == null) {
            return null;
        }
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.getActive())
                .build();
    }

    public static AuthResponse.UserSummary toUserSummary(User user) {
        if (user == null) {
            return null;
        }
        return AuthResponse.UserSummary.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}

