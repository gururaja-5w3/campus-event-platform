package edu.nbkrist.campusvibe.repository;

import edu.nbkrist.campusvibe.model.AppUser;
import edu.nbkrist.campusvibe.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmailIgnoreCase(String email);
    boolean existsByRole(Role role);
}
