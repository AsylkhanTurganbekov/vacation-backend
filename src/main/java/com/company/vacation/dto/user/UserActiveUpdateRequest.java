package com.company.vacation.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserActiveUpdateRequest {

    @NotNull
    private Boolean active;
}
