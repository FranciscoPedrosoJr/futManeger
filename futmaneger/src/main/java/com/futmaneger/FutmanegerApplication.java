package com.futmaneger;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.futmaneger.infrastructure.persistence.jpa")
//@EntityScan(basePackages = "com.futmaneger.infrastructure.persistence.entity")
public class FutmanegerApplication {

	public static void main(String[] args) {

		SpringApplication.run(FutmanegerApplication.class, args);
	}

}
