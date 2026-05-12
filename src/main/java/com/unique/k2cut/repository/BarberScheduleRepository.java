package com.unique.k2cut.repository;

import com.unique.k2cut.domain.entity.BarberSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BarberScheduleRepository extends JpaRepository<BarberSchedule, UUID> {
    List<BarberSchedule> findByBarberId(UUID barberId);
    Optional<BarberSchedule> findByBarberIdAndDayOfWeek(UUID barberId, Integer dayOfWeek);
}
