package com.company.vacation.service.impl;

import com.company.vacation.dto.auth.AuthResponse;
import com.company.vacation.dto.auth.LoginRequest;
import com.company.vacation.dto.auth.RegisterRequest;
import com.company.vacation.dto.user.UserResponse;
import com.company.vacation.entity.User;
import com.company.vacation.exception.BusinessException;
import com.company.vacation.mapper.UserMapper;
import com.company.vacation.repository.UserRepository;
import com.company.vacation.security.AppUserDetails;
import com.company.vacation.security.JwtTokenProvider;
import com.company.vacation.service.AuthContextService;
import com.company.vacation.service.AuthService;
import com.company.vacation.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final AuthContextService authContextService;
    private final AuditLogService auditLogService;

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        AppUserDetails principal = (AppUserDetails) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new BusinessException("Authenticated user not found"));
        UserResponse userResponse = userMapper.toResponse(user);
        return AuthResponse.builder()
                .accessToken(jwtTokenProvider.generateToken(principal))
                .tokenType("Bearer")
                .user(userResponse)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BusinessException("Email is already registered");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setDepartment(request.getDepartment());
        user.setPosition(request.getPosition());
        user.setActive(true);
        user = userRepository.save(user);
        auditLogService.log("USER", user.getId(), "REGISTERED", user.getId(), user.getEmail());

        AppUserDetails principal = new AppUserDetails(user);
        return AuthResponse.builder()
                .accessToken(jwtTokenProvider.generateToken(principal))
                .tokenType("Bearer")
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    public AuthResponse me() {
        User user = authContextService.currentUser();
        AppUserDetails principal = new AppUserDetails(user);
        return AuthResponse.builder()
                .accessToken(jwtTokenProvider.generateToken(principal))
                .tokenType("Bearer")
                .user(userMapper.toResponse(user))
                .build();
    }
}
