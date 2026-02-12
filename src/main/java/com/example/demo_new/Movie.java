package com.example.demo_new;

import java.util.Objects;

public class Movie {
    private final int id;
    private String title;
    private String description;
    private int durationMinutes;
    private int ageRestriction;
    private double ticketPrice;

    public Movie(int id, String title, String description, int durationMinutes, int ageRestriction, double ticketPrice) {
        if(id <=0 ) throw new IllegalArgumentException("id must be positive");
        if(title == null) throw new IllegalArgumentException("title is required");
        if(description == null) throw new IllegalArgumentException("description is required ");
        if (durationMinutes <= 0) throw new IllegalArgumentException("durationMinutes must be positive");
        if (ageRestriction < 0) throw new IllegalArgumentException("ageRegistration cannot be negative");
        if (ticketPrice < 0) throw new IllegalArgumentException("ticketPrice cannot be negative");

        this.id = id;
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.ageRestriction = ageRestriction;
        this.ticketPrice = ticketPrice;


    }
    // Геттеры
    public int getId() { return id; }
    public String getTitle() { return title;}
    public String getDescription(){return description;}
    public int getDurationMinutes() {return durationMinutes;}
    public int getAgeRestriction() {return ageRestriction;}
    public double getTicketPrice() {return ticketPrice;}

    // Сеттеры
    public void setTitle(String title){
        if (title == null) throw new IllegalArgumentException("title is required");
        this.title = title;
    }
    public void setDescription(String description){
        if(description == null)throw new IllegalArgumentException("description is required");
        this.description = description;
    }
    public void setDurationMinutes(int durationMinutes) {
        if (durationMinutes <= 0) throw new IllegalArgumentException("durationMinutes must be positive");
        this.durationMinutes = durationMinutes;
    }
    public void setAgeRegistration(int ageRegistration) {
        if (ageRegistration < 0) throw new IllegalArgumentException("ageRegistration cannot be negative");
        this.ageRestriction = ageRegistration;

    }
    public void setTicketPrice(double ticketPrice) {
        if(ticketPrice <0) throw new IllegalArgumentException("ticketPrice cannot be negative");
        this.ticketPrice = ticketPrice;
    }

    //methods
    public boolean isAllowedForAge(int viewerAge){
        return viewerAge >= ageRestriction;
    }

    public double priceForTickets(int quantity){
        if( quantity <=0) throw new IllegalArgumentException("quantity must be positive");
        return ticketPrice * quantity;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", title='"+ title + '\'' +
                "description="+ description +
                ", durationMinutes="+ durationMinutes +
                ", ageRestriction=" + ageRestriction +
                ", tickerPrice=" + ticketPrice+
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(!( o instanceof Movie)) return false;
        Movie movie = (Movie) o;
        return id == movie.id;

    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
