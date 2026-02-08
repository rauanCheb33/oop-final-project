package com.example.demo_new;

/** DTO for POST /movies (no id, DB generates it). */
public class MovieCreateRequest {
    private String title;
    private String description;
    private int durationMinutes;
    private int ageRestriction;
    private double ticketPrice;

    public MovieCreateRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public int getAgeRestriction() { return ageRestriction; }
    public void setAgeRestriction(int ageRestriction) { this.ageRestriction = ageRestriction; }

    public double getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(double ticketPrice) { this.ticketPrice = ticketPrice; }
}
