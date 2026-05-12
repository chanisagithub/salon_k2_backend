package com.unique.k2cut.dto;

import java.time.LocalTime;

public record AvailableSlotDTO(
    LocalTime startTime,
    boolean isAvailable
) {}
