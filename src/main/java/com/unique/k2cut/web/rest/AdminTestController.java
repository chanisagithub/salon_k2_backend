package com.unique.k2cut.web.rest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminTestController {

    @GetMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> testAdminAccess() {
        return Map.of("status", "Success", "message", "You have admin access!");
    }
}
