package com.example.demo_new;

/** DTO for POST /cinemas */
public class CinemaCreateRequest {
    private String name;

    public CinemaCreateRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
