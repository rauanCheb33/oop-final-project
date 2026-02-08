package com.example.demo_new;

import java.util.Objects;

public class Viewer {
    private final int id;
    private String name;
    private int age;
    private double balance;

    public Viewer(int id, String name, int age, double balance) {
        if (id <= 0) throw new IllegalArgumentException("id must be positive");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
        if (age < 0) throw new IllegalArgumentException("age cannot be negative");
        if (balance < 0) throw new IllegalArgumentException("balance cannot be negative");

        this.id = id;
        this.name = name;
        this.age = age;
        this.balance = balance;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public double getBalance() { return balance; }

    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
        this.name = name;
    }

    public void setAge(int age) {
        if (age < 0) throw new IllegalArgumentException("age cannot be negative");
        this.age = age;
    }

    public void setBalance(double balance) {
        if (balance < 0) throw new IllegalArgumentException("balance cannot be negative");
        this.balance = balance;
    }

    public void topUp(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
        this.balance += amount;
    }

    public boolean canWatch(Movie movie) {
        return movie.isAllowedForAge(age);
    }

    public boolean pay(double amount) {
        if (amount < 0) throw new IllegalArgumentException("amount cannot be negative");
        if (balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Viewer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", balance=" + balance +
                '}';
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
