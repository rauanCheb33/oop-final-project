package com.example.demo_new;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movies")
public class MovieController {

    private final DBmanager db = new DBmanager();

    @GetMapping
    public List<Movie> getAll() {
        return db.getAllMovies();
    }

    @GetMapping("/{id}")
    public Movie getById(@PathVariable int id) {
        return db.findMovieById(id).orElseThrow(() -> new NotFoundException("Movie", id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Movie create(@RequestBody MovieCreateRequest req) {
        validateMovie(req.getTitle(), req.getDescription(), req.getDurationMinutes(), req.getAgeRestriction(), req.getTicketPrice());
        return db.createMovie(req);
    }

    @PutMapping("/{id}")
    public Movie update(@PathVariable int id, @RequestBody MovieUpdateRequest req) {
        validateMovie(req.getTitle(), req.getDescription(), req.getDurationMinutes(), req.getAgeRestriction(), req.getTicketPrice());
        boolean updated = db.updateMovie(id, req);
        if (!updated) throw new NotFoundException("Movie", id);
        return db.findMovieById(id).orElseThrow(() -> new NotFoundException("Movie", id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id) {
        boolean deleted = db.deleteMovie(id);
        if (!deleted) throw new NotFoundException("Movie", id);
    }

    private static void validateMovie(String title, String description, int durationMinutes, int ageRestriction, double ticketPrice) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title is required");
        if (description == null) throw new IllegalArgumentException("description is required");
        if (durationMinutes <= 0) throw new IllegalArgumentException("durationMinutes must be positive");
        if (ageRestriction < 0) throw new IllegalArgumentException("ageRestriction cannot be negative");
        if (ticketPrice < 0) throw new IllegalArgumentException("ticketPrice cannot be negative");
    }
}
