package edu.nbkrist.campusvibe.controller;

import edu.nbkrist.campusvibe.model.Booking;
import edu.nbkrist.campusvibe.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService service;
    public BookingController(BookingService service) { this.service = service; }

    public record BookingRequest(Long eventId) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Booking book(@RequestBody BookingRequest req, Authentication auth) {
        if (req.eventId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "eventId is required.");
        try { return service.book(auth.getName(), req.eventId()); }
        catch (IllegalArgumentException e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()); }
    }

    @GetMapping("/mine")
    public List<Booking> mine(Authentication auth) { return service.myBookings(auth.getName()); }
}
