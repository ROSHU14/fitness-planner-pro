package com.fitness.fitnessplanner.controller;

import com.fitness.fitnessplanner.model.UserStreak;
import com.fitness.fitnessplanner.repository.UserStreakRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/streak")
@CrossOrigin(origins = "*")
public class StreakController {

    @Autowired
    private UserStreakRepository streakRepository;

    @GetMapping
    public ResponseEntity<?> getStreak() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            UserStreak streak = streakRepository.findByUserEmail(email).orElse(new UserStreak());
            if (streak.getUserEmail() == null) {
                streak.setUserEmail(email);
                streak.setCurrentStreak(0);
                streak.setLongestStreak(0);
                streakRepository.save(streak);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("currentStreak", streak.getCurrentStreak());
            response.put("longestStreak", streak.getLongestStreak());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}