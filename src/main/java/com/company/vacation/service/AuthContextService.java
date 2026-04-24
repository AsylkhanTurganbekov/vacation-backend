package com.company.vacation.service;

import com.company.vacation.entity.User;
import com.company.vacation.entity.enums.Role;

public interface AuthContextService {

    Long currentUserId();

    Role currentUserRole();

    User currentUser();
}
