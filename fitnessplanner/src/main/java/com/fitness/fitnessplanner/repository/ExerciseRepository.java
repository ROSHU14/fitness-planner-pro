package com.fitness.fitnessplanner.repository;

import com.fitness.fitnessplanner.model.Exercise;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ExerciseRepository extends MongoRepository<Exercise, String> {
    List<Exercise> findByCategory(String category);
    List<Exercise> findByMuscleGroup(String muscleGroup);
    List<Exercise> findByDifficultyLessThanEqual(Integer difficulty);
}