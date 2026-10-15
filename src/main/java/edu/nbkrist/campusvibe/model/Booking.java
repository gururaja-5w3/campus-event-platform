package edu.nbkrist.campusvibe.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings", uniqueConstraints = @UniqueConstraint(columnNames = {"ticketCode"}))
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String ticketCode = UUID.randomUUID().toString();

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private AppUser student;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private CampusEvent event;

    @Column(nullable = false)
    private LocalDateTime bookedAt = LocalDateTime.now();

    public Booking() {}
    public Booking(AppUser student, CampusEvent event) {
        this.student = student;
        this.event = event;
    }
    public Long getId() { return id; }
    public String getTicketCode() { return ticketCode; }
    public AppUser getStudent() { return student; }
    public void setStudent(AppUser student) { this.student = student; }
    public CampusEvent getEvent() { return event; }
    public void setEvent(CampusEvent event) { this.event = event; }
    public LocalDateTime getBookedAt() { return bookedAt; }
}
