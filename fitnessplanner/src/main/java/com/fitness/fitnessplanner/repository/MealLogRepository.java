package com.fitness.fitnessplanner.repository;

import com.fitness.fitnessplanner.model.MealLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDate;
import java.util.List;

public interface MealLogRepository extends MongoRepository<MealLog, String> {
    List<MealLog> findByUserEmailOrderByDateDesc(String userEmail);
    List<MealLog> findByUserEmailAndDateBetween(String userEmail, LocalDate start, LocalDate end);
    List<MealLog> findByUserEmailAndDate(String userEmail, LocalDate date);
}