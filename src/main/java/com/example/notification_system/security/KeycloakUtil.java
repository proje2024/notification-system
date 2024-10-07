package com.example.notification_system.security;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
public class KeycloakUtil {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String keycloakRealm;

    @Value("${keycloak.resource}")
    private String keycloakClientId;

    @Value("${keycloak.credentials.secret}")
    private String keycloakClientSecret;

    public String getUsernameFromToken() {
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        Jwt jwt = (Jwt) authentication.getCredentials();
        return jwt != null ? jwt.getClaimAsString("preferred_username") : null;
    }

    public String getEmailFromToken() {
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        Jwt jwt = (Jwt) authentication.getCredentials();
        return jwt != null ? jwt.getClaimAsString("email") : null;
    }

    public String getUserIdFromToken() {
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        Jwt jwt = (Jwt) authentication.getCredentials();
        return jwt != null ? jwt.getSubject() : null;
    }

    public boolean isTokenValid() {
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        Jwt jwt = (Jwt) authentication.getCredentials();
        Instant expiresAt = jwt != null ? jwt.getExpiresAt() : null;
        return expiresAt != null && expiresAt.isAfter(Instant.now());
    }

    public String getEmailByUserId(String userId) {
        String adminToken = getAdminAccessToken();
        String url = keycloakServerUrl + "/admin/realms/" + keycloakRealm + "/users/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("email").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Keycloak yanıtı çözümlenemedi", e);
        }
    }
    
    public String getUserIdFromEmail(String email) {
        String adminToken = getAdminAccessToken();
        String url = keycloakServerUrl + "/admin/realms/" + keycloakRealm + "/users?email=" + email;
    
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);
    
        HttpEntity<String> entity = new HttpEntity<>(headers);
    
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.isArray() && root.size() > 0) {
                return root.get(0).path("id").asText();
            } else {
                throw new RuntimeException("Kullanıcı bulunamadı: " + email);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Keycloak yanıtı çözümlenemedi", e);
        }
    }

    private String getAdminAccessToken() {
        String tokenUrl = keycloakServerUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", keycloakClientId);
        body.add("client_secret", keycloakClientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && responseBody.containsKey("access_token")) {
            return (String) responseBody.get("access_token");
        } else {
            throw new RuntimeException("Keycloak'tan erişim tokenı alınamadı");
        }
    }
}
