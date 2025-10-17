package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication // Remova o (exclude = { DataSourceAutoConfiguration.class })
@EnableJpaRepositories(basePackages = "com.example.repository")
public class AvaliaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AvaliaApplication.class, args);
    }
}