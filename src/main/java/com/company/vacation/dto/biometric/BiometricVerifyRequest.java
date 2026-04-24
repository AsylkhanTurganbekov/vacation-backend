package com.company.vacation.dto.biometric;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BiometricVerifyRequest {

    @NotNull
    @Positive
    private Long employeeId;

    @NotNull
    @Positive
    private Long tripEventId;

    @NotBlank
    @Size(max = 1000000)
    private String imageBase64;

    @Size(max = 1000)
    private String imageUrl;
}
