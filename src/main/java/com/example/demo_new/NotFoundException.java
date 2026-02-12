package com.example.demo_new;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String entity, int id) {
        super(entity + " with id=" + id + " not found");
    }
}
