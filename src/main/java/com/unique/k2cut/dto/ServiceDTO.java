package com.unique.k2cut.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ServiceDTO(
    UUID id,
    String name,
    String description,
    Integer durationMinutes,
    BigDecimal price,
    Boolean isActive
) {}
