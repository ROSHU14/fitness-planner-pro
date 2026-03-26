package com.fitness.fitnessplanner.repository;

import com.fitness.fitnessplanner.model.Achievement;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AchievementRepository extends MongoRepository<Achievement, String> {
    List<Achievement> findByUserEmailOrderByEarnedDateDesc(String userEmail);
    boolean existsByUserEmailAndBadgeName(String userEmail, String badgeName);
    long countByUserEmail(String userEmail);
}