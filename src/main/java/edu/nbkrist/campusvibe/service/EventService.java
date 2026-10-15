package edu.nbkrist.campusvibe.service;

import edu.nbkrist.campusvibe.model.*;
import edu.nbkrist.campusvibe.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {
    private final EventRepository events;
    private final UserRepository users;
    public EventService(EventRepository events, UserRepository users) { this.events = events; this.users = users; }

    public List<CampusEvent> publicEvents() { return events.findByApprovedTrueOrderByEventDateAsc(); }
    public List<CampusEvent> pendingEvents() { return events.findByApprovedFalseOrderByCreatedAtDesc(); }

    @Transactional
    public CampusEvent create(String email, String title, String category, String description,
                              String venue, LocalDateTime date, Integer capacity, BigDecimal price) {
        if (date == null || date.isBefore(LocalDateTime.now())) throw new IllegalArgumentException("Event date must be in the future.");
        if (capacity == null || capacity < 1) throw new IllegalArgumentException("Capacity must be at least 1.");
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Ticket price cannot be negative.");
        AppUser organizer = users.findByEmailIgnoreCase(email).orElseThrow();
        CampusEvent event = new CampusEvent();
        event.setTitle(title); event.setCategory(category); event.setDescription(description);
        event.setVenue(venue); event.setEventDate(date); event.setCapacity(capacity);
        event.setTicketPrice(price); event.setOrganizer(organizer); event.setApproved(organizer.getRole() == Role.ADMIN);
        return events.save(event);
    }

    @Transactional
    public CampusEvent approve(Long id) {
        CampusEvent event = events.findById(id).orElseThrow(() -> new IllegalArgumentException("Event not found."));
        event.setApproved(true);
        return event;
    }

    @Transactional
    public void delete(Long id) {
        if (!events.existsById(id)) throw new IllegalArgumentException("Event not found.");
        events.deleteById(id);
    }

    public long approvedCount() { return events.countByApprovedTrue(); }
    public long totalCount() { return events.count(); }
}
