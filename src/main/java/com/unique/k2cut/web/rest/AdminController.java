package com.unique.k2cut.web.rest;

import com.unique.k2cut.domain.entity.AppointmentStatus;
import com.unique.k2cut.dto.AppointmentResponse;
import com.unique.k2cut.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AppointmentService appointmentService;

    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> testAdminAccess() {
        return ResponseEntity.ok(Map.of("status", "Success", "message", "You have admin access!"));
    }

    @GetMapping("/appointments")
    public ResponseEntity<Page<AppointmentResponse>> getAppointments(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) AppointmentStatus status,
            @PageableDefault(size = 10, sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(appointmentService.searchAppointments(query, date, status, pageable));
    }

    @PatchMapping("/appointments/{id}/status")
    public ResponseEntity<AppointmentResponse> updateStatus(
            @PathVariable UUID id,
            @RequestParam AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, status));
    }
}
