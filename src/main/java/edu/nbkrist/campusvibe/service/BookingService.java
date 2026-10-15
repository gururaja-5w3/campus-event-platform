package edu.nbkrist.campusvibe.service;

import edu.nbkrist.campusvibe.model.*;
import edu.nbkrist.campusvibe.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookings;
    private final EventRepository events;
    private final UserRepository users;
    public BookingService(BookingRepository bookings, EventRepository events, UserRepository users) {
        this.bookings = bookings; this.events = events; this.users = users;
    }

    @Transactional
    public Booking book(String email, Long eventId) {
        AppUser student = users.findByEmailIgnoreCase(email).orElseThrow();
        if (student.getRole() == Role.ORGANIZER) throw new IllegalArgumentException("Organizer accounts cannot book tickets in this demo.");
        CampusEvent event = events.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Event not found."));
        if (!event.isApproved()) throw new IllegalArgumentException("This event is not approved yet.");
        if (bookings.existsByStudentAndEvent(student, event)) throw new IllegalArgumentException("You already booked this event.");
        if (bookings.countByEvent(event) >= event.getCapacity()) throw new IllegalArgumentException("This event is sold out.");
        return bookings.save(new Booking(student, event));
    }

    public List<Booking> myBookings(String email) {
        AppUser student = users.findByEmailIgnoreCase(email).orElseThrow();
        return bookings.findByStudentOrderByBookedAtDesc(student);
    }
}
