package com.company.vacation.service.impl;

import com.company.vacation.dto.common.PagedResponse;
import com.company.vacation.dto.user.UserActiveUpdateRequest;
import com.company.vacation.dto.user.UserRequest;
import com.company.vacation.dto.user.UserResponse;
import com.company.vacation.dto.user.UserUpdateRequest;
import com.company.vacation.entity.User;
import com.company.vacation.entity.enums.Role;
import com.company.vacation.exception.ApiValidationException;
import com.company.vacation.exception.BusinessException;
import com.company.vacation.exception.NotFoundException;
import com.company.vacation.mapper.UserMapper;
import com.company.vacation.repository.UserRepository;
import com.company.vacation.service.AuditLogService;
import com.company.vacation.service.AuthContextService;
import com.company.vacation.service.UserService;
import com.company.vacation.specification.UserSpecification;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final long MAX_AVATAR_SIZE_BYTES = 5L * 1024 * 1024;

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final AuthContextService authContextService;

    @Value("${app.storage.avatar-dir:${APP_STORAGE_AVATAR_DIR:/app/uploads/avatars}}")
    private String avatarDir;

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

    @Override
    @Transactional
    public UserResponse uploadAvatar(Long id, MultipartFile file) {
        User user = findUser(id);
        ensureAvatarAccess(user);
        validateAvatarFile(file);

        deleteAvatarFileIfExists(user.getAvatarFileName());
        String storedFileName = buildAvatarFileName(file.getOriginalFilename());
        Path storagePath = avatarStoragePath().resolve(storedFileName);
        try {
            Files.createDirectories(storagePath.getParent());
            Files.copy(file.getInputStream(), storagePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new BusinessException("Failed to store avatar file");
        }

        user.setAvatarFileName(storedFileName);
        auditLogService.log("USER", user.getId(), "AVATAR_UPLOADED", authContextService.currentUserId(), storedFileName);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void deleteAvatar(Long id) {
        User user = findUser(id);
        ensureAvatarAccess(user);
        deleteAvatarFileIfExists(user.getAvatarFileName());
        user.setAvatarFileName(null);
        auditLogService.log("USER", user.getId(), "AVATAR_DELETED", authContextService.currentUserId(), null);
    }

    @Override
    public Resource getAvatar(Long id) {
        User user = findUser(id);
        if (user.getAvatarFileName() == null || user.getAvatarFileName().isBlank()) {
            throw new NotFoundException("Avatar not found for user with id " + id);
        }

        Path avatarPath = avatarStoragePath().resolve(user.getAvatarFileName()).normalize();
        try {
            Resource resource = new UrlResource(avatarPath.toUri());
            if (!resource.exists()) {
                throw new NotFoundException("Avatar file not found for user with id " + id);
            }
            return resource;
        } catch (MalformedURLException exception) {
            throw new BusinessException("Failed to read avatar file");
        }
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

    private void ensureAvatarAccess(User user) {
        if (authContextService.currentUserRole() == Role.EMPLOYEE
                && !authContextService.currentUserId().equals(user.getId())) {
            throw new BusinessException("Employees can only modify their own avatar");
        }
    }

    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiValidationException("Avatar file must be provided");
        }
        if (file.getSize() > MAX_AVATAR_SIZE_BYTES) {
            throw new ApiValidationException("Avatar file size must not exceed 5 MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ApiValidationException("Avatar file must be an image");
        }
    }

    private String buildAvatarFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null) {
            int extensionIndex = originalFilename.lastIndexOf('.');
            if (extensionIndex >= 0) {
                extension = originalFilename.substring(extensionIndex).toLowerCase();
            }
        }
        return UUID.randomUUID() + extension;
    }

    private void deleteAvatarFileIfExists(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(avatarStoragePath().resolve(fileName));
        } catch (IOException exception) {
            throw new BusinessException("Failed to delete avatar file");
        }
    }

    private Path avatarStoragePath() {
        return Path.of(avatarDir).toAbsolutePath().normalize();
    }
}
