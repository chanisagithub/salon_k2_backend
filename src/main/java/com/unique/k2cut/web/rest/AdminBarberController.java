package com.unique.k2cut.web.rest;

import com.unique.k2cut.dto.BarberDTO;
import com.unique.k2cut.dto.BarberRequest;
import com.unique.k2cut.dto.BarberScheduleDTO;
import com.unique.k2cut.dto.BarberScheduleRequest;
import com.unique.k2cut.service.BarberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/barbers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminBarberController {

    private final BarberService barberService;

    @GetMapping
    public ResponseEntity<List<BarberDTO>> getAll() {
        return ResponseEntity.ok(barberService.getAllBarbers());
    }

    @PostMapping
    public ResponseEntity<BarberDTO> create(@Valid @RequestBody BarberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(barberService.createBarber(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BarberDTO> update(@PathVariable UUID id, @Valid @RequestBody BarberRequest request) {
        return ResponseEntity.ok(barberService.updateBarber(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        barberService.deactivateBarber(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/schedules")
    public ResponseEntity<List<BarberScheduleDTO>> getSchedules(@PathVariable UUID id) {
        return ResponseEntity.ok(barberService.getSchedules(id));
    }

    @PutMapping("/{id}/schedules")
    public ResponseEntity<List<BarberScheduleDTO>> setSchedules(
            @PathVariable UUID id,
            @Valid @RequestBody List<BarberScheduleRequest> requests) {
        return ResponseEntity.ok(barberService.setSchedules(id, requests));
    }
}
