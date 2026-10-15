package edu.nbkrist.campusvibe.service;

import edu.nbkrist.campusvibe.model.AppUser;
import edu.nbkrist.campusvibe.model.Role;
import edu.nbkrist.campusvibe.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    public AuthService(UserRepository users, PasswordEncoder encoder) {
        this.users = users; this.encoder = encoder;
    }

    public AppUser register(String name, String email, String password) {
        String normalizedEmail = email.trim().toLowerCase();
        if (users.findByEmailIgnoreCase(normalizedEmail).isPresent())
            throw new IllegalArgumentException("An account with this email already exists.");
        if (password == null || password.length() < 8)
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        return users.save(new AppUser(name.trim(), normalizedEmail, encoder.encode(password), Role.STUDENT));
    }
}
