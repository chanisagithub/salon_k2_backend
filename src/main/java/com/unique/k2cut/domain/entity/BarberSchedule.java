package com.unique.k2cut.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "barber_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BarberSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id", nullable = false)
    private Barber barber;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek; // 1 (Monday) to 7 (Sunday)

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "is_working_day")
    private Boolean isWorkingDay = true;
}
