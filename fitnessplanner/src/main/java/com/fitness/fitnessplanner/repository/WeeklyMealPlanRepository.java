package com.fitness.fitnessplanner.repository;

import com.fitness.fitnessplanner.model.WeeklyMealPlan;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface WeeklyMealPlanRepository extends MongoRepository<WeeklyMealPlan, String> {
    Optional<WeeklyMealPlan> findByUserEmailAndWeekStartDate(String userEmail, LocalDate weekStartDate);
}