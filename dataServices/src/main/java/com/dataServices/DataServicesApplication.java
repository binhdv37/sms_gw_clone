package com.dataServices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.services", "com.dataServices"})
@EnableJpaRepositories(basePackages = {"com.repositories"})
@EntityScan(basePackages = {"com.models"})
public class DataServicesApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataServicesApplication.class);
    }
}
