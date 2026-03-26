package com.fitness.fitnessplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FitnessplannerApplication {
	public static void main(String[] args) {
		SpringApplication.run(FitnessplannerApplication.class, args);
	}
}