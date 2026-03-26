package com.fitness.fitnessplanner.controller;

import com.fitness.fitnessplanner.dto.FitnessPlanResponse;
import com.fitness.fitnessplanner.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/fitness")
@CrossOrigin(origins = "*")
public class FitnessController {

    @Autowired
    private UserService userService;

    @GetMapping("/plan")
    public ResponseEntity<?> getFitnessPlan() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            FitnessPlanResponse plan = userService.generateFitnessPlan(email);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", plan);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/account")
    public ResponseEntity<?> deleteAccount() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            boolean deleted = userService.deleteUser(email);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Account deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/bmi")
    public ResponseEntity<?> getUserBMI() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            Map<String, Object> bmiData = userService.getUserBMI(email);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", bmiData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}