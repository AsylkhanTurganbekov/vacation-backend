package com.company.vacation.service.impl;

import com.company.vacation.entity.User;
import com.company.vacation.entity.enums.Role;
import com.company.vacation.exception.NotFoundException;
import com.company.vacation.repository.UserRepository;
import com.company.vacation.security.AppUserDetails;
import com.company.vacation.service.AuthContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthContextServiceImpl implements AuthContextService {

    private final UserRepository userRepository;

    @Override
    public Long currentUserId() {
        return principal().getId();
    }

    @Override
    public Role currentUserRole() {
        return principal().getRole();
    }

    @Override
    public User currentUser() {
        return userRepository.findById(currentUserId())
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));
    }

    private AppUserDetails principal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails principal)) {
            throw new NotFoundException("Authenticated principal not found");
        }
        return principal;
    }
}
