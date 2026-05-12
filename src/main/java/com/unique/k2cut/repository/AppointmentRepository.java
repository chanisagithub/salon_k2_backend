package com.unique.k2cut.repository;

import com.unique.k2cut.domain.entity.Appointment;
import com.unique.k2cut.domain.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID>, JpaSpecificationExecutor<Appointment> {
    List<Appointment> findByCustomerId(UUID customerId);
    List<Appointment> findByBarberIdAndStartTimeBetween(UUID barberId, OffsetDateTime start, OffsetDateTime end);
    
    boolean existsByBarberIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
            UUID barberId, 
            Collection<AppointmentStatus> statuses, 
            OffsetDateTime endTime, 
            OffsetDateTime startTime
    );
}
