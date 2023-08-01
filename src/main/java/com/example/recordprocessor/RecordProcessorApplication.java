package com.example.recordprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.recordprocessor.repository")
public class RecordProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecordProcessorApplication.class, args);
    }

}
