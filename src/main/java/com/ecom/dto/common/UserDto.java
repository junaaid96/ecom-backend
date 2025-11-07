package com.ecom.dto.common;

import com.ecom.entity.enums.UserRole;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserDto {
    Long id;
    String username;
    String email;
    UserRole role;
    Boolean active;
}

