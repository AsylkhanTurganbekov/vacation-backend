package com.company.vacation.service;

import com.company.vacation.dto.common.PagedResponse;
import com.company.vacation.dto.user.UserActiveUpdateRequest;
import com.company.vacation.dto.user.UserRequest;
import com.company.vacation.dto.user.UserResponse;
import com.company.vacation.dto.user.UserUpdateRequest;
import com.company.vacation.entity.enums.Role;
import org.springframework.data.domain.Pageable;

public interface UserService {

    PagedResponse<UserResponse> getUsers(String queryText, Role role, String department, Boolean active, Pageable pageable);

    UserResponse getUser(Long id);

    UserResponse createUser(UserRequest request);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    UserResponse updateActive(Long id, UserActiveUpdateRequest request);
}
