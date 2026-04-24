package com.company.vacation.dto.trip;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTripRequest {

    @NotNull
    @Positive
    private Long employeeId;

    @NotBlank
    @Size(max = 500)
    private String purpose;

    @NotBlank
    @Size(max = 500)
    private String destinationAddress;

    @NotNull
    @FutureOrPresent
    private LocalDateTime plannedStartDateTime;

    @NotNull
    @FutureOrPresent
    private LocalDateTime plannedEndDateTime;
}
