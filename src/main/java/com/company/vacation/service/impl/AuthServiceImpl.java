package com.company.vacation.service.impl;

import com.company.vacation.dto.auth.AuthResponse;
import com.company.vacation.dto.auth.LoginRequest;
import com.company.vacation.dto.auth.LogoutRequest;
import com.company.vacation.dto.auth.RefreshTokenRequest;
import com.company.vacation.dto.auth.RegisterRequest;
import com.company.vacation.dto.user.UserResponse;
import com.company.vacation.entity.RefreshToken;
import com.company.vacation.entity.User;
import com.company.vacation.exception.BusinessException;
import com.company.vacation.mapper.UserMapper;
import com.company.vacation.repository.RefreshTokenRepository;
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
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final AuthContextService authContextService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        userRepository.findByEmailIgnoreCase(request.getEmail()).ifPresent(user -> {
            if (!user.isActive()) {
                throw new BusinessException("User account is inactive");
            }
        });
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        AppUserDetails principal = (AppUserDetails) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new BusinessException("Authenticated user not found"));
        return issueAuthResponse(user);
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

        return issueAuthResponse(user);
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

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException("Invalid refresh token"));
        if (refreshToken.isRevoked() || refreshToken.isExpired()) {
            throw new BusinessException("Refresh token is expired or revoked");
        }

        User user = refreshToken.getUser();
        if (!user.isActive()) {
            throw new BusinessException("User account is inactive");
        }

        refreshToken.setRevokedAt(java.time.LocalDateTime.now());
        return issueAuthResponse(user);
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        refreshTokenRepository.findByToken(request.getRefreshToken()).ifPresent(token -> {
            if (!token.isRevoked()) {
                token.setRevokedAt(java.time.LocalDateTime.now());
            }
        });
    }

    private AuthResponse issueAuthResponse(User user) {
        AppUserDetails principal = new AppUserDetails(user);
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setExpiresAt(java.time.LocalDateTime.now()
                .plus(java.time.Duration.ofMillis(jwtTokenProvider.getRefreshExpirationMs())));
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(jwtTokenProvider.generateToken(principal))
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .user(userMapper.toResponse(user))
                .build();
    }
}
