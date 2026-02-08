package com.example.demo_new;

/** DTO for POST /cinemas/{cinemaId}/book */
public class BookingRequest {
    private int viewerId;
    private int movieId;
    private int quantity;

    public BookingRequest() {}

    public int getViewerId() { return viewerId; }
    public void setViewerId(int viewerId) { this.viewerId = viewerId; }

    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
