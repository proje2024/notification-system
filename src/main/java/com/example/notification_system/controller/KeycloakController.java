package com.example.notification_system.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.notification_system.service.KeycloakService;

@RestController
@RequestMapping("/token")
public class KeycloakController {
    private final KeycloakService keycloakService;

    @Autowired
    public KeycloakController(KeycloakService keycloakService) {
        this.keycloakService = keycloakService;
    }

    @PostMapping
    public Map<String, Object> getToken(@RequestParam String code, @RequestParam String currentPath) {
        try {
            return keycloakService.getToken(code, currentPath);
        } catch (Exception e) {
            return Map.of("error", "Token alınamadı: " + e.getMessage());
        }
    }
    

    @PostMapping("/refresh")
    public Map<String, Object> refreshToken(@RequestParam String refreshToken) {
        try {
            return keycloakService.refreshToken(refreshToken);
        } catch (Exception e) {
            return Map.of("error", "Reflesh token alınamadı: " + e.getMessage());
        }
    }
}
