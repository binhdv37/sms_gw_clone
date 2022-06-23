package com.consumers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = { "com.services", "com.consumers" })
@EnableJpaRepositories(basePackages = { "com.repositories", "com.consumers.repositories" })
@EntityScan(basePackages = { "com.models" })
public class ConsumersApplication {
	public static void main(String[] args) {
		SpringApplication.run(ConsumersApplication.class);
	}
}
