package com.company.vacation.service;

import com.company.vacation.dto.auth.AuthResponse;
import com.company.vacation.dto.auth.LoginRequest;
import com.company.vacation.dto.auth.RegisterRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse register(RegisterRequest request);

    AuthResponse me();
}
