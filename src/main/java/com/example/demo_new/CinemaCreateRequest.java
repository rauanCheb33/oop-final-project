package com.example.demo_new;

/** DTO for POST /cinemas */
public class CinemaCreateRequest {
    private String name;
    private String city;
    private String address;

    public CinemaCreateRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
