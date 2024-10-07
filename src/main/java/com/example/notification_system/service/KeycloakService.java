package com.example.notification_system.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@Service
public class KeycloakService {

    private final String TOKEN_ENDPOINT = System.getenv("KEYCLOAK_HOST") + ":" + System.getenv("KEYCLOAK_PORT") +
            "/realms/" + System.getenv("KEYCLOAK_REALM") + "/protocol/openid-connect/token";
    private final String CLIENT_ID = System.getenv("KEYCLOAK_CLIENT");
    private final String CLIENT_SECRET = System.getenv("KEYCLOAK_SECRET");
    private final String REDIRECT_URI_BASE = System.getenv("REDIRECT_URI");

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> getToken(String code, String currentPath) throws Exception {
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

        String redirectUri = REDIRECT_URI_BASE + currentPath;
        System.out.println("Redirect URI: " + redirectUri);
        String form = "client_id=" + URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8) +
                      "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, StandardCharsets.UTF_8) +
                      "&grant_type=" + URLEncoder.encode("authorization_code", StandardCharsets.UTF_8) +
                      "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8) +
                      "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(TOKEN_ENDPOINT))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .timeout(Duration.ofSeconds(10))
            .POST(HttpRequest.BodyPublishers.ofString(form))
            .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<String, Object> responseBody = objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
                return responseBody;
            } else {
                throw new RuntimeException("Keycloak'tan token alınamadı: " + response.body());
            }
        } catch (java.net.http.HttpTimeoutException e) {
            throw new RuntimeException("Zaman aşımı hatası: " + e.getMessage(), e);
        } catch (java.io.IOException e) {
            throw new RuntimeException("IO hatası: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            throw new RuntimeException("İstek kesildi: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> refreshToken(String refreshToken) throws Exception {
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

        String form = "client_id=" + URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8) +
                      "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, StandardCharsets.UTF_8) +
                      "&grant_type=" + URLEncoder.encode("refresh_token", StandardCharsets.UTF_8) +
                      "&refresh_token=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(TOKEN_ENDPOINT))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .timeout(Duration.ofSeconds(10))
            .POST(HttpRequest.BodyPublishers.ofString(form))
            .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<String, Object> responseBody = objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
                return responseBody;
            } else {
                throw new RuntimeException("Keycloak'tan token yenilenemedi: " + response.body());
            }
        } catch (java.net.http.HttpTimeoutException e) {
            throw new RuntimeException("Zaman aşımı hatası: " + e.getMessage(), e);
        } catch (java.io.IOException e) {
            throw new RuntimeException("IO hatası: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            throw new RuntimeException("İstek kesildi: " + e.getMessage(), e);
        }
    }
}

