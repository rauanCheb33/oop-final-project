package com.example.demo_new;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cinemas")
public class CinemaController {

    private final DBmanager db = new DBmanager();

    // ---- CRUD cinemas ----

    @GetMapping
    public List<Cinema> getAll() {
        return db.getAllCinemas();
    }

    @GetMapping("/{id}")
    public Cinema getById(@PathVariable int id) {
        return db.findCinemaById(id).orElseThrow(() -> new NotFoundException("Cinema", id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Cinema create(@RequestBody CinemaCreateRequest req) {
        validateCinema(req.getName());
        return db.createCinema(req);
    }

    @PutMapping("/{id}")
    public Cinema update(@PathVariable int id, @RequestBody CinemaUpdateRequest req) {
        validateCinema(req.getName());
        boolean updated = db.updateCinema(id, req);
        if (!updated) throw new NotFoundException("Cinema", id);
        return db.findCinemaById(id).orElseThrow(() -> new NotFoundException("Cinema", id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id) {
        boolean deleted = db.deleteCinema(id);
        if (!deleted) throw new NotFoundException("Cinema", id);
    }

    // ---- Schedule / Booking (extra but nice for defence) ----

    /** List schedule (movies + available seats) for one cinema */
    @GetMapping("/{cinemaId}/schedule")
    public List<DBmanager.ScheduleItem> getSchedule(@PathVariable int cinemaId) {
        return db.getCinemaSchedule(cinemaId);
    }

    /** Add/update schedule item (movie + seats) */
    @PostMapping("/{cinemaId}/schedule")
    @ResponseStatus(HttpStatus.CREATED)
    public void upsertSchedule(@PathVariable int cinemaId, @RequestBody ScheduleUpsertRequest req) {
        if (req.getMovieId() <= 0) throw new IllegalArgumentException("movieId must be positive");
        if (req.getSeats() < 0) throw new IllegalArgumentException("seats cannot be negative");
        db.upsertScheduleItem(cinemaId, req.getMovieId(), req.getSeats());
    }

    /** Remove a movie from a cinema schedule */
    @DeleteMapping("/{cinemaId}/schedule/{movieId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteScheduleItem(@PathVariable int cinemaId, @PathVariable int movieId) {
        boolean deleted = db.deleteScheduleItem(cinemaId, movieId);
        if (!deleted) throw new NotFoundException("ScheduleItem", cinemaId + ":" + movieId);
    }

    /** Book tickets (uses Cinema.bookTickets + Viewer.pay logic, then persists via JDBC). */
    @PostMapping("/{cinemaId}/book")
    public DBmanager.BookingResult book(@PathVariable int cinemaId, @RequestBody BookingRequest req) {
        if (req.getViewerId() <= 0) throw new IllegalArgumentException("viewerId must be positive");
        if (req.getMovieId() <= 0) throw new IllegalArgumentException("movieId must be positive");
        if (req.getQuantity() <= 0) throw new IllegalArgumentException("quantity must be positive");
        return db.bookTickets(cinemaId, req.getViewerId(), req.getMovieId(), req.getQuantity());
    }

    private static void validateCinema(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
    }
}
