package com.unique.k2cut.dto;

import com.unique.k2cut.domain.entity.AppointmentStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentResponse(
    UUID id,
    UUID customerId,
    String customerName,
    UUID barberId,
    String barberName,
    UUID serviceId,
    String serviceName,
    OffsetDateTime startTime,
    OffsetDateTime endTime,
    AppointmentStatus status,
    String notes
) {}
