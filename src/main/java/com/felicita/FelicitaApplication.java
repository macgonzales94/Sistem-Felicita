package com.felicita;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class FelicitaApplication {

    public static void main(String[] args) {
        SpringApplication.run(FelicitaApplication.class, args);
    }
}