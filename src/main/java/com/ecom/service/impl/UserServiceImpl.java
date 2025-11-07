package com.ecom.service.impl;

import com.ecom.dto.common.UserDto;
import com.ecom.entity.User;
import com.ecom.entity.enums.UserRole;
import com.ecom.exception.BadRequestException;
import com.ecom.exception.ResourceNotFoundException;
import com.ecom.mapper.UserMapper;
import com.ecom.repository.UserRepository;
import com.ecom.security.SecurityUtils;
import com.ecom.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public UserDto updateUserRole(Long id, UserRole role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        user.setRole(role);
        User saved = userRepository.save(user);
        return UserMapper.toUserDto(saved);
    }

    @Override
    @Transactional
    public UserDto updateUserActiveStatus(Long id, Boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        
        // Prevent self-deactivation
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId != null && currentUserId.equals(id) && Boolean.FALSE.equals(active)) {
            throw new BadRequestException("You cannot deactivate your own account");
        }
        
        user.setActive(active);
        User saved = userRepository.save(user);
        return UserMapper.toUserDto(saved);
    }
}

