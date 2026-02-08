package com.example.demo_new;

/** DTO for PUT /cinemas/{id} */
public class CinemaUpdateRequest {
    private String name;

    public CinemaUpdateRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
