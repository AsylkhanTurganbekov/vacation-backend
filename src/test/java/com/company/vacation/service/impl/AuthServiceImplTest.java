package com.company.vacation.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.company.vacation.dto.auth.RegisterRequest;
import com.company.vacation.entity.User;
import com.company.vacation.entity.enums.Role;
import com.company.vacation.mapper.UserMapper;
import com.company.vacation.repository.RefreshTokenRepository;
import com.company.vacation.repository.UserRepository;
import com.company.vacation.security.JwtTokenProvider;
import com.company.vacation.service.AuditLogService;
import com.company.vacation.service.AuthContextService;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private UserMapper userMapper;
    @Mock private AuthContextService authContextService;
    @Mock private AuditLogService auditLogService;
    @InjectMocks private AuthServiceImpl authService;

    @Test
    void publicRegistrationAlwaysCreatesEmployeeEvenWhenAdminIsRequested() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Untrusted caller");
        request.setEmail("caller@example.com");
        request.setPassword("Password123!");
        request.setRole(Role.ADMIN);
        when(userRepository.existsByEmailIgnoreCase(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            setId(saved, 10L);
            return saved;
        });
        when(jwtTokenProvider.generateRefreshToken()).thenReturn("refresh");
        when(jwtTokenProvider.getRefreshExpirationMs()).thenReturn(1_000L);
        when(jwtTokenProvider.generateToken(any())).thenReturn("access");

        authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        org.mockito.Mockito.verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRole()).isEqualTo(Role.EMPLOYEE);
    }

    private void setId(User user, Long id) throws Exception {
        Field field = User.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(user, id);
    }
}
