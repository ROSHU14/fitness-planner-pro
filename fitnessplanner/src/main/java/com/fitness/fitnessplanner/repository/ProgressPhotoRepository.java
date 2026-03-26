package com.fitness.fitnessplanner.repository;

import com.fitness.fitnessplanner.model.ProgressPhoto;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ProgressPhotoRepository extends MongoRepository<ProgressPhoto, String> {
    List<ProgressPhoto> findByUserEmailOrderByDateDesc(String userEmail);
}