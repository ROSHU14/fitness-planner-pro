package com.fitness.fitnessplanner.repository;

import com.fitness.fitnessplanner.model.SleepLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SleepLogRepository extends MongoRepository<SleepLog, String> {
    List<SleepLog> findByUserEmailOrderByDateDesc(String userEmail);
    Optional<SleepLog> findByUserEmailAndDate(String userEmail, LocalDate date);
}