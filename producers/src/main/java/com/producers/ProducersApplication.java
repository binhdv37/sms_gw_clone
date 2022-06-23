package com.producers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = { "com.services", "com.producers" })
@EnableJpaRepositories(basePackages = { "com.repositories", "com.producers.repository" })
@EntityScan(basePackages = { "com.models" })
public class ProducersApplication {
	public static void main(String[] args) {
		SpringApplication.run(ProducersApplication.class);
	}
}
