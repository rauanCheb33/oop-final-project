package com.example.demo_new;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cinemas")
public class CinemaController {

    private final DBmanager db = new DBmanager();

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

    private static void validateCinema(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
    }
}
