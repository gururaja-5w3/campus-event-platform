package edu.nbkrist.campusvibe.controller;

import edu.nbkrist.campusvibe.model.CampusEvent;
import edu.nbkrist.campusvibe.service.EventService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final EventService service;
    public AdminController(EventService service) { this.service = service; }

    @GetMapping("/dashboard")
    public Map<String, Long> dashboard() {
        return Map.of("totalEvents", service.totalCount(), "approvedEvents", service.approvedCount(),
                "pendingEvents", (long) service.pendingEvents().size());
    }

    @GetMapping("/events/pending")
    public List<CampusEvent> pending() { return service.pendingEvents(); }

    @PostMapping("/events/{id}/approve")
    public CampusEvent approve(@PathVariable Long id) {
        try { return service.approve(id); }
        catch (IllegalArgumentException e) { throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, e.getMessage()); }
    }

    @DeleteMapping("/events/{id}")
    public void delete(@PathVariable Long id) {
        try { service.delete(id); }
        catch (IllegalArgumentException e) { throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, e.getMessage()); }
    }
}
