package com.fitness.fitnessplanner.repository;

import com.fitness.fitnessplanner.model.UserStreak;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserStreakRepository extends MongoRepository<UserStreak, String> {
    Optional<UserStreak> findByUserEmail(String userEmail);
}