package com.company.vacation.controller;

import com.company.vacation.dto.common.PagedResponse;
import com.company.vacation.dto.user.UserActiveUpdateRequest;
import com.company.vacation.dto.user.UserRequest;
import com.company.vacation.dto.user.UserResponse;
import com.company.vacation.dto.user.UserUpdateRequest;
import com.company.vacation.entity.enums.Role;
import com.company.vacation.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public PagedResponse<UserResponse> getUsers(@org.springframework.web.bind.annotation.RequestParam(required = false, name = "q") String queryText,
                                                @org.springframework.web.bind.annotation.RequestParam(required = false) Role role,
                                                @org.springframework.web.bind.annotation.RequestParam(required = false) String department,
                                                @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean active,
                                                @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return userService.getUsers(queryText, role, department, active, pageable);
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @GetMapping("/{id}/avatar")
    public ResponseEntity<Resource> getAvatar(@PathVariable Long id) {
        Resource avatar = userService.getAvatar(id);
        MediaType mediaType = MediaTypeFactory.getMediaType(avatar)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(avatar);
    }

    @PostMapping
    public UserResponse createUser(@Valid @RequestBody UserRequest request) {
        return userService.createUser(request);
    }

    @PostMapping("/{id}/avatar")
    public UserResponse uploadAvatar(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return userService.uploadAvatar(id, file);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return userService.updateUser(id, request);
    }

    @PatchMapping("/{id}/active")
    public UserResponse updateActive(@PathVariable Long id, @Valid @RequestBody UserActiveUpdateRequest request) {
        return userService.updateActive(id, request);
    }

    @DeleteMapping("/{id}/avatar")
    public ResponseEntity<Void> deleteAvatar(@PathVariable Long id) {
        userService.deleteAvatar(id);
        return ResponseEntity.noContent().build();
    }
}
