package com.fitness.fitnessplanner.controller;

import com.fitness.fitnessplanner.model.WaterIntake;
import com.fitness.fitnessplanner.repository.WaterIntakeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/water")
@CrossOrigin(origins = "*")
public class WaterController {

    @Autowired
    private WaterIntakeRepository waterIntakeRepository;

    @PostMapping("/add")
    public ResponseEntity<?> addWater(@RequestBody Map<String, Object> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            LocalDate today = LocalDate.now();
            Double liters = Double.parseDouble(request.get("liters").toString());

            WaterIntake water = waterIntakeRepository.findByUserEmailAndDate(email, today)
                    .orElse(new WaterIntake());

            double currentLiters = water.getLiters() != null ? water.getLiters() : 0;
            double newLiters = currentLiters + liters;

            if (newLiters < 0) {
                newLiters = 0;
            }

            water.setUserEmail(email);
            water.setDate(today);
            water.setLiters(newLiters);
            water.setCups((int) Math.round(newLiters / 0.24));

            WaterIntake saved = waterIntakeRepository.save(water);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Water intake updated");
            response.put("data", saved);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getWaterStats() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            WaterIntake today = waterIntakeRepository.findByUserEmailAndDate(email, LocalDate.now())
                    .orElse(null);

            double liters = today != null && today.getLiters() != null ? today.getLiters() : 0;

            Map<String, Object> stats = new HashMap<>();
            stats.put("today", liters);
            stats.put("goal", 3.0);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}