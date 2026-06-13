package com.unique.k2cut.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ServiceRequest(
    @NotBlank String name,
    String description,
    @NotNull @Positive Integer durationMinutes,
    @NotNull @PositiveOrZero BigDecimal price,
    Boolean isActive
) {}
