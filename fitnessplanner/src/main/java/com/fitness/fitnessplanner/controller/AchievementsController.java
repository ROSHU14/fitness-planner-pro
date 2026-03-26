package com.fitness.fitnessplanner.controller;

import com.fitness.fitnessplanner.model.Achievement;
import com.fitness.fitnessplanner.repository.AchievementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/achievements")
@CrossOrigin(origins = "*")
public class AchievementsController {

    @Autowired
    private AchievementRepository achievementRepository;

    @GetMapping
    public ResponseEntity<?> getAchievements() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            List<Achievement> achievements = achievementRepository.findByUserEmailOrderByEarnedDateDesc(email);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", achievements);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}