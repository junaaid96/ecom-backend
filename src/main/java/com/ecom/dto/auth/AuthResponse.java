package com.ecom.dto.auth;

import com.ecom.entity.enums.UserRole;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthResponse {
    String token;
    String tokenType;
    Long expiresIn;
    UserSummary user;

    @Value
    @Builder
    public static class UserSummary {
        Long id;
        String username;
        String email;
        UserRole role;
    }
}

