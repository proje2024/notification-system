package com.example.notification_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.notification_system.repository")
@EnableScheduling
public class NotificationSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationSystemApplication.class, args);
    }
}
