package com.unique.k2cut.service;

import com.unique.k2cut.dto.BarberDTO;
import com.unique.k2cut.repository.BarberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BarberService {

    private final BarberRepository barberRepository;

    public List<BarberDTO> getAllActiveBarbers() {
        return barberRepository.findAllByIsActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private BarberDTO mapToDTO(com.unique.k2cut.domain.entity.Barber barber) {
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
}
