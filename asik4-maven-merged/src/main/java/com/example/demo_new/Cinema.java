package com.example.demo_new;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;
import java.util.stream.Collectors;

public class Cinema {
    private final int id;
    private String name;

    /**
     * Schedule / seats are stored in DB (table cinema_movies) and loaded on demand.
     * We keep the original in-memory map to reuse your OOP logic, but we hide it from JSON.
     */
    @JsonIgnore
    private final Map<Movie, Integer> availableSeatsByMovie = new LinkedHashMap<>();

    public Cinema(int id, String name) {
        if (id <= 0) throw new IllegalArgumentException("id must be positive");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
        this.name = name;
    }

    public void clearSchedule() {
        availableSeatsByMovie.clear();
    }

    public void addMovie(Movie movie, int seats) {
        if (movie == null) throw new IllegalArgumentException("movie is required");
        if (seats < 0) throw new IllegalArgumentException("seats cannot be negative");
        availableSeatsByMovie.put(movie, seats);
    }

    public int getAvailableSeats(Movie movie) {
        Integer seats = availableSeatsByMovie.get(movie);
        return seats == null ? 0 : seats;
    }

    public Map<Movie, Integer> getSchedule() {
        return Collections.unmodifiableMap(availableSeatsByMovie);
    }

    public void showSchedule() {
        System.out.println("=== Cinema: " + name + " ===");
        if (availableSeatsByMovie.isEmpty()) {
            System.out.println("No movies yet.");
            return;
        }
        for (Map.Entry<Movie, Integer> entry : availableSeatsByMovie.entrySet()) {
            Movie movie = entry.getKey();
            int seats = entry.getValue();
            System.out.println(movie.getTitle() + " (id=" + movie.getId() + ", age=" + movie.getAgeRestriction() + "+, price=" + movie.getTicketPrice() + ") -> seats: " + seats);
        }
    }

    public String bookTickets(Viewer viewer, Movie movie, int quantity) {
        if (viewer == null) return "Booking failed: viewer is null";
        if (movie == null) return "Booking failed: movie is null";
        if (quantity <= 0) return "Booking failed: quantity must be positive";

        if (!availableSeatsByMovie.containsKey(movie)) {
            return "Booking failed: movie is not available in this cinema";
        }

        if (!viewer.canWatch(movie)) {
            return "Booking failed: age restriction! (" + movie.getAgeRestriction() + "+)";
        }

        int availableSeats = availableSeatsByMovie.get(movie);
        if (availableSeats < quantity) {
            return "Booking failed: not enough seats (available " + availableSeats + ")";
        }

        double totalCost = movie.priceForTickets(quantity);
        if (!viewer.pay(totalCost)) {
            return "Booking failed: not enough money";
        }

        availableSeatsByMovie.put(movie, availableSeats - quantity);
        return "Booking success: " + viewer.getName() + " bought " + quantity +
                " ticket(s) for '" + movie.getTitle() + "' total=" + totalCost;
    }

    public Movie searchMovieByTitle(String title) {
        if (title == null) return null;
        for (Movie movie : availableSeatsByMovie.keySet()) {
            if (movie.getTitle().equalsIgnoreCase(title)) {
                return movie;
            }
        }
        return null;
    }

    // фильтр по максимальной цене
    public List<Movie> filterMoviesByMaxPrice(double maxPrice) {
        return availableSeatsByMovie.keySet().stream()
                .filter(movie -> movie.getTicketPrice() <= maxPrice)
                .collect(Collectors.toList());
    }

    public List<Movie> getMoviesSortedByTitle() {
        List<Movie> movieList = new ArrayList<>(availableSeatsByMovie.keySet());
        movieList.sort(Comparator.comparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER));
        return movieList;
    }

    public List<Movie> getMoviesSortedByPrice() {
        List<Movie> movieList = new ArrayList<>(availableSeatsByMovie.keySet());
        movieList.sort(Comparator.comparingDouble(Movie::getTicketPrice));
        return movieList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cinema)) return false;
        Cinema cinema = (Cinema) o;
        return id == cinema.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
