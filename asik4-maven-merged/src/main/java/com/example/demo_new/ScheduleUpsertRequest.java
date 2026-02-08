package com.example.demo_new;

/** DTO for POST /cinemas/{cinemaId}/schedule */
public class ScheduleUpsertRequest {
    private int movieId;
    private int seats;

    public ScheduleUpsertRequest() {}

    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }

    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }
}
