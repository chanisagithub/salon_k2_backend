package com.unique.k2cut.web.rest;

import com.unique.k2cut.dto.*;
import com.unique.k2cut.service.AppointmentService;
import com.unique.k2cut.service.BarberService;
import com.unique.k2cut.service.ServiceService;
import com.unique.k2cut.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/booking")
@RequiredArgsConstructor
public class BookingController {

    private final ServiceService serviceService;
    private final BarberService barberService;
    private final AppointmentService appointmentService;
    private final UserService userService;

    @GetMapping("/services")
    public ResponseEntity<List<ServiceDTO>> getServices() {
        return ResponseEntity.ok(serviceService.getAllActiveServices());
    }

    @GetMapping("/barbers")
    public ResponseEntity<List<BarberDTO>> getBarbers() {
        return ResponseEntity.ok(barberService.getAllActiveBarbers());
    }

    @GetMapping("/slots")
    public ResponseEntity<List<AvailableSlotDTO>> getAvailableSlots(
            @RequestParam UUID barberId,
            @RequestParam UUID serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(appointmentService.getAvailableSlots(barberId, serviceId, date));
    }

    @PostMapping("/appointments")
    public ResponseEntity<AppointmentResponse> createAppointment(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AppointmentRequest request) {
        
        UUID customerId = UUID.fromString(jwt.getSubject());
        
        // Sync user from JWT if they don't exist
        userService.getOrCreateUser(
                customerId,
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("given_name"),
                jwt.getClaimAsString("family_name")
        );

        return ResponseEntity.ok(appointmentService.createAppointment(customerId, request));
    }

    @GetMapping("/my-appointments")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments(@AuthenticationPrincipal Jwt jwt) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(appointmentService.getCustomerAppointments(customerId));
    }

    @DeleteMapping("/appointments/{id}")
    public ResponseEntity<Void> cancelAppointment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        appointmentService.cancelAppointment(customerId, id);
        return ResponseEntity.noContent().build();
    }
}
