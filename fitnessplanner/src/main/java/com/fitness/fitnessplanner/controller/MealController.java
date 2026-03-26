package com.fitness.fitnessplanner.controller;

import com.fitness.fitnessplanner.model.MealLog;
import com.fitness.fitnessplanner.repository.MealLogRepository;
import com.fitness.fitnessplanner.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meals")
@CrossOrigin(origins = "*")
public class MealController {

    @Autowired
    private MealLogRepository mealLogRepository;

    @Autowired
    private UserService userService;  // Add this

    @PostMapping("/log")
    public ResponseEntity<?> logMeal(@RequestBody Map<String, Object> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            MealLog meal = new MealLog();
            meal.setUserEmail(email);
            meal.setMealType((String) request.get("mealType"));
            meal.setFoodName((String) request.get("foodName"));
            meal.setQuantity(Double.parseDouble(request.get("quantity").toString()));
            meal.setUnit((String) request.get("unit"));
            meal.setCalories(Integer.parseInt(request.get("calories").toString()));

            if (request.get("protein") != null) {
                meal.setProtein(Double.parseDouble(request.get("protein").toString()));
            }
            if (request.get("carbs") != null) {
                meal.setCarbs(Double.parseDouble(request.get("carbs").toString()));
            }
            if (request.get("fats") != null) {
                meal.setFats(Double.parseDouble(request.get("fats").toString()));
            }

            MealLog saved = mealLogRepository.save(meal);

            // ========== ADD THIS LINE - Update streak ==========
            userService.updateStreak(email, true);
            // ==================================================

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Meal logged successfully");
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
    public ResponseEntity<?> getTodayMeals() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            List<MealLog> meals = mealLogRepository.findByUserEmailAndDate(email, LocalDate.now());

            int totalCalories = meals.stream().mapToInt(MealLog::getCalories).sum();
            double totalProtein = meals.stream().mapToDouble(m -> m.getProtein() != null ? m.getProtein() : 0).sum();
            double totalCarbs = meals.stream().mapToDouble(m -> m.getCarbs() != null ? m.getCarbs() : 0).sum();
            double totalFats = meals.stream().mapToDouble(m -> m.getFats() != null ? m.getFats() : 0).sum();

            Map<String, Object> summary = new HashMap<>();
            summary.put("totalCalories", totalCalories);
            summary.put("totalProtein", totalProtein);
            summary.put("totalCarbs", totalCarbs);
            summary.put("totalFats", totalFats);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", meals);
            response.put("summary", summary);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getMealHistory() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            List<MealLog> meals = mealLogRepository.findByUserEmailOrderByDateDesc(email);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", meals);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}