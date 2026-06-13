package com.unique.k2cut.service;

import com.unique.k2cut.domain.entity.Barber;
import com.unique.k2cut.domain.entity.BarberSchedule;
import com.unique.k2cut.domain.entity.User;
import com.unique.k2cut.dto.BarberDTO;
import com.unique.k2cut.dto.BarberRequest;
import com.unique.k2cut.dto.BarberScheduleDTO;
import com.unique.k2cut.dto.BarberScheduleRequest;
import com.unique.k2cut.exception.BookingException;
import com.unique.k2cut.exception.ResourceNotFoundException;
import com.unique.k2cut.repository.BarberRepository;
import com.unique.k2cut.repository.BarberScheduleRepository;
import com.unique.k2cut.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BarberService {

    private final BarberRepository barberRepository;
    private final BarberScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    public List<BarberDTO> getAllActiveBarbers() {
        return barberRepository.findAllByIsActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /** Admin view: includes inactive barbers. */
    public List<BarberDTO> getAllBarbers() {
        return barberRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public BarberDTO createBarber(BarberRequest request) {
        // Barbers are backed by a profile User row (one-to-one).
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(resolveEmail(request));
        user = userRepository.save(user);

        Barber barber = new Barber();
        barber.setUser(user);
        barber.setBio(request.bio());
        barber.setProfileImageUrl(request.profileImageUrl());
        barber.setIsActive(request.isActive() == null ? Boolean.TRUE : request.isActive());
        return mapToDTO(barberRepository.save(barber));
    }

    @Transactional
    public BarberDTO updateBarber(UUID id, BarberRequest request) {
        Barber barber = barberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barber not found"));

        User user = barber.getUser();
        if (user != null) {
            user.setFirstName(request.firstName());
            user.setLastName(request.lastName());
            if (request.email() != null && !request.email().isBlank()) {
                user.setEmail(request.email());
            }
            userRepository.save(user);
        }

        barber.setBio(request.bio());
        barber.setProfileImageUrl(request.profileImageUrl());
        if (request.isActive() != null) {
            barber.setIsActive(request.isActive());
        }
        return mapToDTO(barberRepository.save(barber));
    }

    /** Soft-delete: deactivates the barber so historical appointments stay intact. */
    @Transactional
    public void deactivateBarber(UUID id) {
        Barber barber = barberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barber not found"));
        barber.setIsActive(false);
        barberRepository.save(barber);
    }

    public List<BarberScheduleDTO> getSchedules(UUID barberId) {
        if (!barberRepository.existsById(barberId)) {
            throw new ResourceNotFoundException("Barber not found");
        }
        return scheduleRepository.findByBarberId(barberId).stream()
                .sorted(Comparator.comparing(BarberSchedule::getDayOfWeek))
                .map(this::mapScheduleToDTO)
                .collect(Collectors.toList());
    }

    /** Replaces the barber's full weekly schedule with the supplied entries. */
    @Transactional
    public List<BarberScheduleDTO> setSchedules(UUID barberId, List<BarberScheduleRequest> requests) {
        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new ResourceNotFoundException("Barber not found"));

        for (BarberScheduleRequest req : requests) {
            if (req.endTime().isBefore(req.startTime()) || req.endTime().equals(req.startTime())) {
                throw new BookingException("End time must be after start time for day " + req.dayOfWeek());
            }
        }

        scheduleRepository.deleteAll(scheduleRepository.findByBarberId(barberId));

        List<BarberSchedule> saved = requests.stream().map(req -> {
            BarberSchedule schedule = new BarberSchedule();
            schedule.setBarber(barber);
            schedule.setDayOfWeek(req.dayOfWeek());
            schedule.setStartTime(req.startTime());
            schedule.setEndTime(req.endTime());
            schedule.setIsWorkingDay(req.isWorkingDay() == null ? Boolean.TRUE : req.isWorkingDay());
            return scheduleRepository.save(schedule);
        }).toList();

        return saved.stream()
                .sorted(Comparator.comparing(BarberSchedule::getDayOfWeek))
                .map(this::mapScheduleToDTO)
                .collect(Collectors.toList());
    }

    private String resolveEmail(BarberRequest request) {
        if (request.email() != null && !request.email().isBlank()) {
            return request.email();
        }
        // Generate a stable placeholder so the unique/not-null constraint holds.
        return "barber-" + UUID.randomUUID() + "@2kcut.local";
    }

    private BarberDTO mapToDTO(Barber barber) {
        String firstName = barber.getUser() != null ? barber.getUser().getFirstName() : "Unknown";
        String lastName = barber.getUser() != null ? barber.getUser().getLastName() : "Barber";

        return new BarberDTO(
                barber.getId(),
                firstName,
                lastName,
                barber.getBio(),
                barber.getProfileImageUrl(),
                barber.getIsActive()
        );
    }

    private BarberScheduleDTO mapScheduleToDTO(BarberSchedule schedule) {
        return new BarberScheduleDTO(
                schedule.getId(),
                schedule.getDayOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getIsWorkingDay()
        );
    }
}
