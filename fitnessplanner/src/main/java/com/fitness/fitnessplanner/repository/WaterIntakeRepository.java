package com.fitness.fitnessplanner.repository;

import com.fitness.fitnessplanner.model.WaterIntake;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface WaterIntakeRepository extends MongoRepository<WaterIntake, String> {
    Optional<WaterIntake> findByUserEmailAndDate(String userEmail, LocalDate date);
    Optional<WaterIntake> findTopByUserEmailOrderByDateDesc(String userEmail);
}