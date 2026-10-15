package edu.nbkrist.campusvibe.service;

import edu.nbkrist.campusvibe.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository users;
    public CustomUserDetailsService(UserRepository users) { this.users = users; }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = users.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new UsernameNotFoundException("No account found for this email"));
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(), user.getPassword(),
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
    }
}
