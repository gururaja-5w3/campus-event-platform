package edu.nbkrist.campusvibe.repository;

import edu.nbkrist.campusvibe.model.AppUser;
import edu.nbkrist.campusvibe.model.CampusEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventRepository extends JpaRepository<CampusEvent, Long> {
    List<CampusEvent> findByApprovedTrueOrderByEventDateAsc();
    List<CampusEvent> findByApprovedFalseOrderByCreatedAtDesc();
    List<CampusEvent> findByOrganizerOrderByCreatedAtDesc(AppUser organizer);
    long countByApprovedTrue();
}
