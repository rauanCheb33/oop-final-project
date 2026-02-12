package com.example.demo_new;

import java.util.Objects;

public class Cinema {
    private final int id;
    private String name;
    private String city;
    private String address;

    public Cinema(int id, String name, String city, String address) {
        if (id <= 0) throw new IllegalArgumentException("id must be positive");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");

        this.id = id;
        this.name = name;
        this.city = city;
        this.address = address;
    }

    public int getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
        this.name = name;
    }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cinema)) return false;
        Cinema cinema = (Cinema) o;
        return id == cinema.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
