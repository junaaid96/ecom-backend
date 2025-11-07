package com.ecom.service;

import com.ecom.dto.auth.AuthResponse;
import com.ecom.dto.auth.LoginRequest;
import com.ecom.dto.auth.RegisterRequest;
import com.ecom.dto.common.UserDto;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserDto getCurrentUser();
}

