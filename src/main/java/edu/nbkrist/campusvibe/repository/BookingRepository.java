package edu.nbkrist.campusvibe.repository;

import edu.nbkrist.campusvibe.model.AppUser;
import edu.nbkrist.campusvibe.model.Booking;
import edu.nbkrist.campusvibe.model.CampusEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsByStudentAndEvent(AppUser student, CampusEvent event);
    List<Booking> findByStudentOrderByBookedAtDesc(AppUser student);
    long countByEvent(CampusEvent event);
}
