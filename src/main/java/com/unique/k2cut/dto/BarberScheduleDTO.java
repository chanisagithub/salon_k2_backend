package com.unique.k2cut.dto;

import java.time.LocalTime;
import java.util.UUID;

public record BarberScheduleDTO(
    UUID id,
    Integer dayOfWeek,
    LocalTime startTime,
    LocalTime endTime,
    Boolean isWorkingDay
) {}
