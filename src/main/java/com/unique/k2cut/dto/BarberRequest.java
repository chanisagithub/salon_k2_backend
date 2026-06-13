package com.unique.k2cut.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record BarberRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @Email String email,
    String bio,
    String profileImageUrl,
    Boolean isActive
) {}
