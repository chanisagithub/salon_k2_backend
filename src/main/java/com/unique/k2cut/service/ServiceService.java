package com.unique.k2cut.service;

import com.unique.k2cut.dto.ServiceDTO;
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

    public ServiceDTO getServiceById(UUID id) {
        return serviceRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new com.unique.k2cut.exception.ResourceNotFoundException("Service not found"));
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
