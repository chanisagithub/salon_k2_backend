package com.unique.k2cut.service;

import com.unique.k2cut.domain.entity.User;
import com.unique.k2cut.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User getOrCreateUser(UUID id, String email, String firstName, String lastName) {
        return userRepository.findById(id).orElseGet(() -> {
            User newUser = new User();
            newUser.setId(id);
            newUser.setEmail(email);
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            return userRepository.save(newUser);
        });
    }

    @Transactional
    public User updateUser(UUID id, String firstName, String lastName, String phoneNumber) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new com.unique.k2cut.exception.ResourceNotFoundException("User not found"));
        
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhoneNumber(phoneNumber);
        
        return userRepository.save(user);
    }
}
