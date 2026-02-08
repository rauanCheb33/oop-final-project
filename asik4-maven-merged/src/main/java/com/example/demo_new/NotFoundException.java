package com.example.demo_new;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String entity, Object id) {
        super(entity + " with id=" + id + " not found");
    }

    public NotFoundException(String entity, int id) {
        this(entity, (Object) id);
    }
}
