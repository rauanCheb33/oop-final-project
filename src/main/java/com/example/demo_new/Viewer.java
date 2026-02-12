package com.example.demo_new;

import java.util.Objects;

public class Viewer {
    private final int id;
    private String fullName;
    private int age;
    private String email;

    public Viewer(int id, String fullName, int age, String email) {
        if (id <= 0) throw new IllegalArgumentException("id must be positive");
        if (fullName == null || fullName.isBlank()) throw new IllegalArgumentException("fullName is required");
        if (age < 0) throw new IllegalArgumentException("age cannot be negative");

        this.id = id;
        this.fullName = fullName;
        this.age = age;
        this.email = email;
    }

    public int getId() { return id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) throw new IllegalArgumentException("fullName is required");
        this.fullName = fullName;
    }

    public int getAge() { return age; }
    public void setAge(int age) {
        if (age < 0) throw new IllegalArgumentException("age cannot be negative");
        this.age = age;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean canWatch(Movie movie) {
        return movie.isAllowedForAge(this.age);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Viewer)) return false;
        Viewer viewer = (Viewer) o;
        return id == viewer.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
