package com.example.notification_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String frontendServiceUrl = System.getenv("FRONTEND_SERVICE_URL");
                String frontendLocalUrl = System.getenv("FRONTEND_LOCAL_URL");
            
                registry.addMapping("/**")
                        .allowedOrigins(frontendServiceUrl, frontendLocalUrl)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("Authorization", "Content-Type", "X-Requested-With", "Accept")
                        .exposedHeaders("Authorization")
                        .allowCredentials(true);
            }
        };
    }
}
