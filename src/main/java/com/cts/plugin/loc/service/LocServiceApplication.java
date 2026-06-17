package com.cts.plugin.loc.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class LocServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LocServiceApplication.class, args);
    }
}

