package com.unique.k2cut.web.rest;

import com.unique.k2cut.dto.ServiceDTO;
import com.unique.k2cut.dto.ServiceRequest;
import com.unique.k2cut.service.ServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/services")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminServiceController {

    private final ServiceService serviceService;

    @GetMapping
    public ResponseEntity<List<ServiceDTO>> getAll() {
        return ResponseEntity.ok(serviceService.getAllServices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceDTO> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceService.getServiceById(id));
    }

    @PostMapping
    public ResponseEntity<ServiceDTO> create(@Valid @RequestBody ServiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceService.createService(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceDTO> update(@PathVariable UUID id, @Valid @RequestBody ServiceRequest request) {
        return ResponseEntity.ok(serviceService.updateService(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        serviceService.deactivateService(id);
        return ResponseEntity.noContent().build();
    }
}
