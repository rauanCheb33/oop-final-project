package com.example.demo_new;

/** DTO for PUT /viewers/{id} */
public class ViewerUpdateRequest {
    private String name;
    private int age;
    private double balance;

    public ViewerUpdateRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
}
