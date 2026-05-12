package com.unique.k2cut.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentRequest(
    @NotNull UUID serviceId,
    @NotNull UUID barberId,
    @NotNull @Future OffsetDateTime startTime,
    String notes
) {}
