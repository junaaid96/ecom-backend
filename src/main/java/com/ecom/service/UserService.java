package com.ecom.service;

import com.ecom.dto.common.UserDto;
import com.ecom.entity.enums.UserRole;

import java.util.List;

public interface UserService {
    List<UserDto> getAllUsers();
    UserDto getUser(Long id);
    UserDto updateUserRole(Long id, UserRole role);
    UserDto updateUserActiveStatus(Long id, Boolean active);
}

