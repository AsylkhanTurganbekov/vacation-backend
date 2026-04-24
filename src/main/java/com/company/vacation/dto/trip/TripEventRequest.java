package com.company.vacation.dto.trip;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TripEventRequest {

    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private BigDecimal latitude;

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private BigDecimal longitude;

    @NotBlank
    @Size(max = 500)
    private String address;

    @NotNull
    @PastOrPresent
    private LocalDateTime eventTime;

    @Size(max = 1000)
    private String comment;

    @Size(max = 1000000)
    private String imageBase64;

    @Size(max = 1000)
    private String imageUrl;
}
