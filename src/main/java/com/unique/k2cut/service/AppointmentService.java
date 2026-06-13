package com.unique.k2cut.service;

import com.unique.k2cut.domain.entity.*;
import com.unique.k2cut.dto.AppointmentRequest;
import com.unique.k2cut.dto.AppointmentResponse;
import com.unique.k2cut.dto.AvailableSlotDTO;
import com.unique.k2cut.exception.BookingException;
import com.unique.k2cut.exception.ResourceNotFoundException;
import com.unique.k2cut.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;
    private final BarberRepository barberRepository;
    private final UserRepository userRepository;
    private final BarberScheduleRepository scheduleRepository;

    @Transactional
    public AppointmentResponse createAppointment(UUID customerId, AppointmentRequest request) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found. Please sync user first."));

        com.unique.k2cut.domain.entity.Service service = serviceRepository.findById(request.serviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        Barber barber = barberRepository.findById(request.barberId())
                .orElseThrow(() -> new ResourceNotFoundException("Barber not found"));

        if (!barber.getIsActive()) {
            throw new BookingException("Barber is not currently active");
        }

        if (request.startTime().isBefore(OffsetDateTime.now())) {
            throw new BookingException("Cannot book an appointment in the past");
        }

        OffsetDateTime endTime = request.startTime().plusMinutes(service.getDurationMinutes());

        // dayOfWeek: 1 (Monday) to 7 (Sunday)
        int dayOfWeek = request.startTime().getDayOfWeek().getValue();
        BarberSchedule schedule = scheduleRepository.findByBarberIdAndDayOfWeek(barber.getId(), dayOfWeek)
                .orElseThrow(() -> new BookingException("Barber does not work on this day (" + request.startTime().getDayOfWeek() + ")"));

        if (!schedule.getIsWorkingDay()) {
            throw new BookingException("Barber is not working on this day");
        }

        // Validate working hours
        if (request.startTime().toLocalTime().isBefore(schedule.getStartTime()) ||
            endTime.toLocalTime().isAfter(schedule.getEndTime())) {
            throw new BookingException("Requested time " + request.startTime().toLocalTime() + " - " + endTime.toLocalTime() + 
                                     " is outside of barber's working hours: " + schedule.getStartTime() + " - " + schedule.getEndTime());
        }

        // Conflict check
        boolean hasConflict = appointmentRepository.existsByBarberIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                barber.getId(),
                List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED),
                endTime,
                request.startTime()
        );

        if (hasConflict) {
            throw new BookingException("Barber is already booked at this time");
        }

        Appointment appointment = new Appointment();
        appointment.setCustomer(customer);
        appointment.setBarber(barber);
        appointment.setService(service);
        appointment.setStartTime(request.startTime());
        appointment.setEndTime(endTime);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setNotes(request.notes());

        appointment = appointmentRepository.save(appointment);

        return mapToResponse(appointment);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> searchAppointments(
            String query, 
            LocalDate date, 
            AppointmentStatus status, 
            Pageable pageable) {
        
        Specification<Appointment> spec = (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (date != null) {
                OffsetDateTime startOfDay = date.atStartOfDay().atOffset(ZoneOffset.UTC);
                OffsetDateTime endOfDay = date.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
                predicates.add(cb.between(root.get("startTime"), startOfDay, endOfDay));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (query != null && !query.isBlank()) {
                String searchPattern = "%" + query.toLowerCase() + "%";
                
                // Search in customer name, barber name, or service name
                Predicate customerFirst = cb.like(cb.lower(root.get("customer").get("firstName")), searchPattern);
                Predicate customerLast = cb.like(cb.lower(root.get("customer").get("lastName")), searchPattern);
                Predicate barberFirst = cb.like(cb.lower(root.get("barber").get("user").get("firstName")), searchPattern);
                Predicate barberLast = cb.like(cb.lower(root.get("barber").get("user").get("lastName")), searchPattern);
                Predicate serviceName = cb.like(cb.lower(root.get("service").get("name")), searchPattern);
                
                predicates.add(cb.or(customerFirst, customerLast, barberFirst, barberLast, serviceName));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return appointmentRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getCustomerAppointments(UUID customerId) {
        return appointmentRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllAppointments(LocalDate date) {
        if (date == null) {
            return appointmentRepository.findAll().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }
        
        OffsetDateTime startOfDay = date.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endOfDay = date.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        return appointmentRepository.findByStartTimeBetween(startOfDay, endOfDay).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentResponse updateStatus(UUID appointmentId, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        
        appointment.setStatus(status);
        return mapToResponse(appointmentRepository.save(appointment));
    }

    @Transactional(readOnly = true)
    public List<AvailableSlotDTO> getAvailableSlots(UUID barberId, UUID serviceId, LocalDate date) {
        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new ResourceNotFoundException("Barber not found"));
        
        com.unique.k2cut.domain.entity.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        int dayOfWeek = date.getDayOfWeek().getValue();
        BarberSchedule schedule = scheduleRepository.findByBarberIdAndDayOfWeek(barberId, dayOfWeek)
                .orElse(null);

        if (schedule == null || !schedule.getIsWorkingDay()) {
            return List.of();
        }

        // Get all appointments for this barber on this day
        OffsetDateTime startOfDay = date.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endOfDay = date.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        List<Appointment> existingAppointments = appointmentRepository.findByBarberIdAndStartTimeBetween(
                barberId, startOfDay, endOfDay
        ).stream()
        .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
        .collect(Collectors.toList());

        List<AvailableSlotDTO> slots = new ArrayList<>();
        LocalTime currentTime = schedule.getStartTime();
        int intervalMinutes = 15; // Precision for start times

        while (currentTime.plusMinutes(service.getDurationMinutes()).isBefore(schedule.getEndTime()) || 
               currentTime.plusMinutes(service.getDurationMinutes()).equals(schedule.getEndTime())) {
            
            LocalTime slotStartTime = currentTime;
            LocalTime slotEndTime = currentTime.plusMinutes(service.getDurationMinutes());
            
            boolean isConflict = false;
            for (Appointment app : existingAppointments) {
                LocalTime appStart = app.getStartTime().toLocalTime();
                LocalTime appEnd = app.getEndTime().toLocalTime();
                
                // Overlap check
                if (slotStartTime.isBefore(appEnd) && slotEndTime.isAfter(appStart)) {
                    isConflict = true;
                    break;
                }
            }

            if (date.equals(LocalDate.now()) && slotStartTime.isBefore(LocalTime.now())) {
                isConflict = true;
            }

            slots.add(new AvailableSlotDTO(slotStartTime, !isConflict));
            currentTime = currentTime.plusMinutes(intervalMinutes);
        }

        return slots;
    }

    @Transactional
    public void cancelAppointment(UUID customerId, UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        
        if (!appointment.getCustomer().getId().equals(customerId)) {
            throw new BookingException("You are not authorized to cancel this appointment");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return;
        }

        if (appointment.getStartTime().isBefore(OffsetDateTime.now().plusHours(2))) {
            throw new BookingException("Appointments can only be cancelled at least 2 hours in advance");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    private AppointmentResponse mapToResponse(Appointment appointment) {
        String barberName = "Unknown Barber";
        if (appointment.getBarber() != null && appointment.getBarber().getUser() != null) {
            barberName = appointment.getBarber().getUser().getFirstName() + " " + appointment.getBarber().getUser().getLastName();
        }

        return new AppointmentResponse(
                appointment.getId(),
                appointment.getCustomer().getId(),
                appointment.getCustomer().getFirstName() + " " + appointment.getCustomer().getLastName(),
                appointment.getBarber().getId(),
                barberName,
                appointment.getService().getId(),
                appointment.getService().getName(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus(),
                appointment.getNotes()
        );
    }
}
