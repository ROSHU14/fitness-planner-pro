package com.fitness.fitnessplanner.repository;

import com.fitness.fitnessplanner.model.StepLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StepLogRepository extends MongoRepository<StepLog, String> {
    List<StepLog> findByUserEmailOrderByDateDesc(String userEmail);
    Optional<StepLog> findByUserEmailAndDate(String userEmail, LocalDate date);
}