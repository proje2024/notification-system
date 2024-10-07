package com.example.notification_system.security;

import java.security.Principal;

public class JwtTokenPrincipal implements Principal {

    private final String username;
    private final String token;

    public JwtTokenPrincipal(String username, String token) {
        this.username = username;
        this.token = token;
    }

    @Override
    public String getName() {
        return this.username;
    }

    public String getToken() {
        return this.token;
    }
}
