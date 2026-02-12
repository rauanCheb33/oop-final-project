package com.example.demo_new;

/** DTO for POST /viewers */
public class ViewerCreateRequest {
    private String fullName;
    private int age;
    private String email;

    public ViewerCreateRequest() {}

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
