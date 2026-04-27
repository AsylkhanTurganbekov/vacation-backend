package com.company.vacation.service.impl;

import com.company.vacation.dto.common.PagedResponse;
import com.company.vacation.dto.user.UserActiveUpdateRequest;
import com.company.vacation.dto.user.UserRequest;
import com.company.vacation.dto.user.UserResponse;
import com.company.vacation.dto.user.UserUpdateRequest;
import com.company.vacation.entity.User;
import com.company.vacation.entity.enums.Role;
import com.company.vacation.exception.BusinessException;
import com.company.vacation.exception.NotFoundException;
import com.company.vacation.mapper.UserMapper;
import com.company.vacation.repository.UserRepository;
import com.company.vacation.service.AuditLogService;
import com.company.vacation.service.AuthContextService;
import com.company.vacation.service.UserService;
import com.company.vacation.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final AuthContextService authContextService;

    @Override
    public PagedResponse<UserResponse> getUsers(String queryText, Role role, String department, Boolean active, Pageable pageable) {
        Specification<User> specification = UserSpecification.filter(queryText, role, department, active);
        return PagedResponse.from(userRepository.findAll(specification, pageable).map(userMapper::toResponse));
    }

    @Override
    public UserResponse getUser(Long id) {
        return userMapper.toResponse(findUser(id));
    }

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {
        ensureUniqueEmail(request.getEmail(), null);
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setDepartment(request.getDepartment());
        user.setPosition(request.getPosition());
        user.setActive(request.getActive());
        user = userRepository.save(user);
        auditLogService.log("USER", user.getId(), "CREATED", authContextService.currentUserId(), user.getEmail());
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = findUser(id);
        ensureUniqueEmail(request.getEmail(), id);
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail().toLowerCase());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        user.setRole(request.getRole());
        user.setDepartment(request.getDepartment());
        user.setPosition(request.getPosition());
        user.setActive(request.getActive());
        auditLogService.log("USER", user.getId(), "UPDATED", authContextService.currentUserId(), user.getEmail());
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateActive(Long id, UserActiveUpdateRequest request) {
        User user = findUser(id);
        user.setActive(request.getActive());
        auditLogService.log("USER", user.getId(), "ACTIVE_UPDATED", authContextService.currentUserId(), request);
        return userMapper.toResponse(user);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id " + id));
    }

    private void ensureUniqueEmail(String email, Long excludedId) {
        userRepository.findByEmailIgnoreCase(email).ifPresent(existing -> {
            if (excludedId == null || !existing.getId().equals(excludedId)) {
                throw new BusinessException("Email is already in use");
            }
        });
    }
}
