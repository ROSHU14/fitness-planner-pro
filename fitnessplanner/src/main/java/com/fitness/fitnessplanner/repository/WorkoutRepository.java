package com.fitness.fitnessplanner.repository;

import com.fitness.fitnessplanner.model.WorkoutLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface WorkoutRepository extends MongoRepository<WorkoutLog, String> {
    List<WorkoutLog> findByUserEmailOrderByDateDesc(String userEmail);
    List<WorkoutLog> findByUserEmailAndDateBetween(String userEmail, LocalDateTime start, LocalDateTime end);
    long countByUserEmailAndDateAfter(String userEmail, LocalDateTime date);
    long countByUserEmail(String userEmail);
    List<WorkoutLog> findTop5ByUserEmailOrderByDateDesc(String userEmail);
}