package com.example.demo_new;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoNewApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoNewApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase() {
        return args -> DBmanager.setupDatabase();
    }
}
