package com.example.demo_new;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/viewers")
public class ViewerController {

    private final DBmanager db = new DBmanager();

    @GetMapping
    public List<Viewer> getAll() {
        return db.getAllViewers();
    }

    @GetMapping("/{id}")
    public Viewer getById(@PathVariable int id) {
        return db.findViewerById(id).orElseThrow(() -> new NotFoundException("Viewer", id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Viewer create(@RequestBody ViewerCreateRequest req) {
        validateViewer(req.getFullName(), req.getAge(), req.getEmail());
        return db.createViewer(req);
    }

    @PutMapping("/{id}")
    public Viewer update(@PathVariable int id, @RequestBody ViewerUpdateRequest req) {
        validateViewer(req.getFullName(), req.getAge(), req.getEmail());
        boolean updated = db.updateViewer(id, req);
        if (!updated) throw new NotFoundException("Viewer", id);
        return db.findViewerById(id).orElseThrow(() -> new NotFoundException("Viewer", id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id) {
        boolean deleted = db.deleteViewer(id);
        if (!deleted) throw new NotFoundException("Viewer", id);
    }

    private static void validateViewer(String fullName, int age, String email) {
        if (fullName == null || fullName.isBlank()) throw new IllegalArgumentException("fullName is required");
        if (age < 0) throw new IllegalArgumentException("age cannot be negative");
        // email is optional (can be null)
    }
}
