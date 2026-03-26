package com.fitness.fitnessplanner.repository;

import com.fitness.fitnessplanner.model.Progress;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDate;
import java.util.List;

public interface ProgressRepository extends MongoRepository<Progress, String> {
    List<Progress> findByUserEmailOrderByDateDesc(String userEmail);
    List<Progress> findByUserEmailOrderByDateAsc(String userEmail);
    List<Progress> findByUserEmailAndDateBetween(String userEmail, LocalDate start, LocalDate end);
    List<Progress> findByUserEmailAndDate(String userEmail, LocalDate date);
}