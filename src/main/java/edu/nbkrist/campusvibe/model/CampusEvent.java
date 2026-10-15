package edu.nbkrist.campusvibe.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "campus_events")
public class CampusEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 140)
    private String title;

    @Column(nullable = false, length = 40)
    private String category;

    @Column(nullable = false, length = 1500)
    private String description;

    @Column(nullable = false, length = 180)
    private String venue;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal ticketPrice = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean approved = false;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private AppUser organizer;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public CampusEvent() {}
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public LocalDateTime getEventDate() { return eventDate; }
    public void setEventDate(LocalDateTime eventDate) { this.eventDate = eventDate; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public BigDecimal getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(BigDecimal ticketPrice) { this.ticketPrice = ticketPrice; }
    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
    public AppUser getOrganizer() { return organizer; }
    public void setOrganizer(AppUser organizer) { this.organizer = organizer; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
