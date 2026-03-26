package com.fitness.fitnessplanner.controller;

import com.fitness.fitnessplanner.model.User;
import com.fitness.fitnessplanner.model.WeeklyMealPlan;
import com.fitness.fitnessplanner.repository.UserRepository;
import com.fitness.fitnessplanner.repository.WeeklyMealPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/meal-plan")
@CrossOrigin(origins = "*")
public class MealPlanController {

    @Autowired
    private WeeklyMealPlanRepository mealPlanRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentMealPlan() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            LocalDate weekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
            WeeklyMealPlan plan = mealPlanRepository.findByUserEmailAndWeekStartDate(email, weekStart).orElse(null);

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

    @PostMapping("/generate")
    public ResponseEntity<?> generateMealPlan() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            LocalDate weekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY);

            // Check if plan already exists
            if (mealPlanRepository.findByUserEmailAndWeekStartDate(email, weekStart).isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Meal plan already exists for this week");
                response.put("data", mealPlanRepository.findByUserEmailAndWeekStartDate(email, weekStart).get());
                return ResponseEntity.ok(response);
            }

            WeeklyMealPlan plan = new WeeklyMealPlan();
            plan.setUserEmail(email);
            plan.setWeekStartDate(weekStart);

            Map<String, WeeklyMealPlan.DailyMeals> weekPlan = new LinkedHashMap<>();
            int totalCalories = 0;
            String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

            // Calculate user's calorie needs
            double bmr;
            if ("female".equalsIgnoreCase(user.getGender())) {
                bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() - 161;
            } else {
                bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() + 5;
            }

            double activityMultiplier;
            switch (user.getWorkoutFrequency().toLowerCase()) {
                case "sedentary": activityMultiplier = 1.2; break;
                case "moderate": activityMultiplier = 1.375; break;
                case "active": activityMultiplier = 1.55; break;
                case "very_active": activityMultiplier = 1.725; break;
                default: activityMultiplier = 1.2;
            }

            double maintenanceCalories = bmr * activityMultiplier;
            double targetCalories;

            switch (user.getWeightGoal().toUpperCase()) {
                case "GAIN": targetCalories = maintenanceCalories + 500; break;
                case "LOSS": targetCalories = maintenanceCalories - 500; break;
                default: targetCalories = maintenanceCalories;
            }

            int dailyCalories = (int) Math.round(targetCalories);

            for (String day : days) {
                WeeklyMealPlan.DailyMeals dailyMeal = new WeeklyMealPlan.DailyMeals();

                // Generate meals based on calorie target
                int breakfastCal = (int) Math.round(dailyCalories * 0.25);
                int lunchCal = (int) Math.round(dailyCalories * 0.35);
                int dinnerCal = (int) Math.round(dailyCalories * 0.30);
                int snackCal = dailyCalories - (breakfastCal + lunchCal + dinnerCal);

                if (user.getWeightGoal().equalsIgnoreCase("GAIN")) {
                    dailyMeal.setBreakfast("🍳 " + breakfastCal + " kcal: 4 eggs, oatmeal, banana, protein shake");
                    dailyMeal.setLunch("🍗 " + lunchCal + " kcal: 250g chicken breast, brown rice, vegetables");
                    dailyMeal.setDinner("🐟 " + dinnerCal + " kcal: 250g salmon, sweet potato, quinoa");
                    dailyMeal.setSnacks("🥜 " + snackCal + " kcal: Trail mix, Greek yogurt, protein bar");
                } else if (user.getWeightGoal().equalsIgnoreCase("LOSS")) {
                    dailyMeal.setBreakfast("🥣 " + breakfastCal + " kcal: Oatmeal with berries, 2 boiled eggs");
                    dailyMeal.setLunch("🥗 " + lunchCal + " kcal: Grilled chicken salad, quinoa");
                    dailyMeal.setDinner("🐟 " + dinnerCal + " kcal: Baked fish, steamed vegetables");
                    dailyMeal.setSnacks("🍎 " + snackCal + " kcal: Apple, Greek yogurt");
                } else {
                    dailyMeal.setBreakfast("🍳 " + breakfastCal + " kcal: 3 eggs, whole grain toast, fruit");
                    dailyMeal.setLunch("🥗 " + lunchCal + " kcal: Chicken bowl with quinoa, vegetables");
                    dailyMeal.setDinner("🦃 " + dinnerCal + " kcal: Turkey breast, sweet potato");
                    dailyMeal.setSnacks("🥛 " + snackCal + " kcal: Greek yogurt, nuts");
                }

                dailyMeal.setCalories(dailyCalories);
                weekPlan.put(day, dailyMeal);
                totalCalories += dailyCalories;
            }

            plan.setWeekPlan(weekPlan);
            plan.setTotalCalories(totalCalories);

            WeeklyMealPlan saved = mealPlanRepository.save(plan);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Weekly meal plan generated!");
            response.put("data", saved);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}