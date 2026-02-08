package com.example.demo_new;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class DemoNewApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoNewApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            // In tests/CI there may be no .env and no DB. For defence you will have .env.
            if (!Files.exists(Path.of(".env"))) {
                System.out.println("[DB] .env not found - skipping DB setup");
                return;
            }
            DBmanager.setupDatabase();
        };
    }
}
