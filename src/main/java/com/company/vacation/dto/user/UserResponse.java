package com.company.vacation.dto.user;

import com.company.vacation.entity.enums.Role;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private String department;
    private String position;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
