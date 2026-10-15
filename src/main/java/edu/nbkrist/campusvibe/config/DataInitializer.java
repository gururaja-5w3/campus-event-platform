package edu.nbkrist.campusvibe.config;

import edu.nbkrist.campusvibe.model.AppUser;
import edu.nbkrist.campusvibe.model.Role;
import edu.nbkrist.campusvibe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner createInitialAdmin(
            UserRepository users,
            PasswordEncoder encoder,
            @Value("${ADMIN_NAME:Campus Admin}") String adminName,
            @Value("${ADMIN_EMAIL:}") String adminEmail,
            @Value("${ADMIN_PASSWORD:}") String adminPassword) {
        return args -> {
            if (adminEmail.isBlank() || adminPassword.isBlank()) {
                System.out.println("Initial admin not configured. Set ADMIN_EMAIL and ADMIN_PASSWORD environment variables.");
                return;
            }
            if (!users.existsByRole(Role.ADMIN)) {
                users.save(new AppUser(adminName, adminEmail.trim().toLowerCase(), encoder.encode(adminPassword), Role.ADMIN));
                System.out.println("Initial CampusVibe admin created for: " + adminEmail);
            }
        };
    }
}
