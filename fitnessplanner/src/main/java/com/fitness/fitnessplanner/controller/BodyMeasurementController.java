package com.fitness.fitnessplanner.controller;

import com.fitness.fitnessplanner.model.BodyMeasurement;
import com.fitness.fitnessplanner.repository.BodyMeasurementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/measurements")
@CrossOrigin(origins = "*")
public class BodyMeasurementController {

    @Autowired
    private BodyMeasurementRepository measurementRepository;

    @PostMapping("/add")
    public ResponseEntity<?> addMeasurement(@RequestBody Map<String, Object> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            BodyMeasurement measurement = new BodyMeasurement();
            measurement.setUserEmail(email);
            measurement.setDate(LocalDate.now());

            if (request.get("chest") != null) {
                measurement.setChest(Double.parseDouble(request.get("chest").toString()));
            }
            if (request.get("waist") != null) {
                measurement.setWaist(Double.parseDouble(request.get("waist").toString()));
            }
            if (request.get("hips") != null) {
                measurement.setHips(Double.parseDouble(request.get("hips").toString()));
            }
            if (request.get("arms") != null) {
                measurement.setArms(Double.parseDouble(request.get("arms").toString()));
            }
            if (request.get("thighs") != null) {
                measurement.setThighs(Double.parseDouble(request.get("thighs").toString()));
            }
            if (request.get("neck") != null) {
                measurement.setNeck(Double.parseDouble(request.get("neck").toString()));
            }

            BodyMeasurement saved = measurementRepository.save(measurement);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Measurements saved successfully");
            response.put("data", saved);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<?> getLatestMeasurements() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            BodyMeasurement latest = measurementRepository.findTopByUserEmailOrderByDateDesc(email).orElse(null);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", latest);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getMeasurementHistory() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            java.util.List<BodyMeasurement> measurements = measurementRepository.findByUserEmailOrderByDateDesc(email);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", measurements);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}