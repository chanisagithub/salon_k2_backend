package com.unique.k2cut.web.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/public")
public class PublicController {

    @GetMapping("/health")
    public Map<String, String> healthCheck() {
        return Map.of("status", "UP", "message", "2KCUT Saloon API is operational");
    }
}
