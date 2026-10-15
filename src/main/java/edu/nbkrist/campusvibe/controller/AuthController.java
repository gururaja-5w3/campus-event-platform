package edu.nbkrist.campusvibe.controller;

import edu.nbkrist.campusvibe.model.AppUser;
import edu.nbkrist.campusvibe.service.AuthService;
import edu.nbkrist.campusvibe.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UserRepository users;
    public AuthController(AuthService authService, UserRepository users) { this.authService = authService; this.users = users; }

    public record RegisterRequest(@NotBlank @Size(max=100) String fullName,
                                  @NotBlank @Email @Size(max=180) String email,
                                  @NotBlank @Size(min=8, max=100) String password) {}
    public record UserView(Long id, String fullName, String email, String role) {}

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserView register(@Valid @RequestBody RegisterRequest req) {
        try { return view(authService.register(req.fullName(), req.email(), req.password())); }
        catch (IllegalArgumentException e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()); }
    }

    @GetMapping("/me")
    public UserView me(Authentication authentication) {
        if (authentication == null) return null;
        return users.findByEmailIgnoreCase(authentication.getName()).map(AuthController::view).orElse(null);
    }

    private static UserView view(AppUser u) { return new UserView(u.getId(), u.getFullName(), u.getEmail(), u.getRole().name()); }
}
