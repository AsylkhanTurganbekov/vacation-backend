package com.company.vacation.dto.auth;

import com.company.vacation.dto.user.UserResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private UserResponse user;
}
