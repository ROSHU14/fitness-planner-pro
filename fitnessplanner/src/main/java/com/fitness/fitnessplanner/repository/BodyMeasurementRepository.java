package com.fitness.fitnessplanner.repository;

import com.fitness.fitnessplanner.model.BodyMeasurement;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface BodyMeasurementRepository extends MongoRepository<BodyMeasurement, String> {
    List<BodyMeasurement> findByUserEmailOrderByDateDesc(String userEmail);
    Optional<BodyMeasurement> findTopByUserEmailOrderByDateDesc(String userEmail);
}