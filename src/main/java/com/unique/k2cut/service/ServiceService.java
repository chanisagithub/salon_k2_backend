package com.unique.k2cut.service;

import com.unique.k2cut.dto.ServiceDTO;
import com.unique.k2cut.dto.ServiceRequest;
import com.unique.k2cut.exception.ResourceNotFoundException;
import com.unique.k2cut.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public List<ServiceDTO> getAllActiveServices() {
        return serviceRepository.findAllByIsActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /** Admin view: includes inactive services. */
    public List<ServiceDTO> getAllServices() {
        return serviceRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ServiceDTO getServiceById(UUID id) {
        return serviceRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
    }

    @Transactional
    public ServiceDTO createService(ServiceRequest request) {
        com.unique.k2cut.domain.entity.Service service = new com.unique.k2cut.domain.entity.Service();
        apply(service, request);
        return mapToDTO(serviceRepository.save(service));
    }

    @Transactional
    public ServiceDTO updateService(UUID id, ServiceRequest request) {
        com.unique.k2cut.domain.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        apply(service, request);
        return mapToDTO(serviceRepository.save(service));
    }

    /** Soft-delete: deactivates the service so historical appointments stay intact. */
    @Transactional
    public void deactivateService(UUID id) {
        com.unique.k2cut.domain.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        service.setIsActive(false);
        serviceRepository.save(service);
    }

    private void apply(com.unique.k2cut.domain.entity.Service service, ServiceRequest request) {
        service.setName(request.name());
        service.setDescription(request.description());
        service.setDurationMinutes(request.durationMinutes());
        service.setPrice(request.price());
        service.setIsActive(request.isActive() == null ? Boolean.TRUE : request.isActive());
    }

    private ServiceDTO mapToDTO(com.unique.k2cut.domain.entity.Service service) {
        return new ServiceDTO(
                service.getId(),
                service.getName(),
                service.getDescription(),
                service.getDurationMinutes(),
                service.getPrice(),
                service.getIsActive()
        );
    }
}
