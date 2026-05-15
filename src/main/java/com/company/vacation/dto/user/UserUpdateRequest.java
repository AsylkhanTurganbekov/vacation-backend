package com.company.vacation.dto.user;

import com.company.vacation.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {

    @NotBlank
    @Size(max = 255)
    private String fullName;

    @Email
    @NotBlank
    private String email;

    @Pattern(regexp = "^\\d{12}$", message = "must contain exactly 12 digits")
    private String iin;

    @Size(min = 8, max = 100)
    private String password;

    @NotNull
    private Role role;

    @Size(max = 255)
    private String department;

    @Size(max = 255)
    private String position;

    @NotNull
    private Boolean active;
}
