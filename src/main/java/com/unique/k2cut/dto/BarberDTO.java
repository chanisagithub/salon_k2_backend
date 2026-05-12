package com.unique.k2cut.dto;

import java.util.UUID;

public record BarberDTO(
    UUID id,
    String firstName,
    String lastName,
    String bio,
    String profileImageUrl,
    Boolean isActive
) {}
