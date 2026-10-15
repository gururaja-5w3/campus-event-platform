package edu.nbkrist.campusvibe.controller;

import edu.nbkrist.campusvibe.model.CampusEvent;
import edu.nbkrist.campusvibe.service.EventService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService service;
    public EventController(EventService service) { this.service = service; }

    public record EventRequest(@NotBlank @Size(max=140) String title,
                               @NotBlank @Size(max=40) String category,
                               @NotBlank @Size(max=1500) String description,
                               @NotBlank @Size(max=180) String venue,
                               @NotNull LocalDateTime eventDate,
                               @NotNull @Min(1) Integer capacity,
                               @NotNull @DecimalMin("0.00") BigDecimal ticketPrice) {}

    @GetMapping
    public List<CampusEvent> list() { return service.publicEvents(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CampusEvent create(@Valid @RequestBody EventRequest req, Authentication auth) {
        try {
            return service.create(auth.getName(), req.title(), req.category(), req.description(),
                    req.venue(), req.eventDate(), req.capacity(), req.ticketPrice());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
