package com.fitness.fitnessplanner.controller;

import com.fitness.fitnessplanner.model.StepLog;
import com.fitness.fitnessplanner.repository.StepLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/steps")
@CrossOrigin(origins = "*")
public class StepsController {

    @Autowired
    private StepLogRepository stepRepository;

    @PostMapping("/log")
    public ResponseEntity<?> logSteps(@RequestBody Map<String, Object> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            StepLog steps = new StepLog();
            steps.setUserEmail(email);
            steps.setDate(LocalDate.now());
            steps.setSteps(Integer.parseInt(request.get("steps").toString()));

            StepLog saved = stepRepository.save(steps);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", saved);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/today")
    public ResponseEntity<?> getTodaySteps() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            StepLog today = stepRepository.findByUserEmailAndDate(email, LocalDate.now()).orElse(null);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", today);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}