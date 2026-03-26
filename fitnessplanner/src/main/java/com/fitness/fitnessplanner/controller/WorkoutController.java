package com.fitness.fitnessplanner.controller;

import com.fitness.fitnessplanner.model.Achievement;
import com.fitness.fitnessplanner.model.WorkoutLog;
import com.fitness.fitnessplanner.repository.WorkoutRepository;
import com.fitness.fitnessplanner.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workouts")
@CrossOrigin(origins = "*")
public class WorkoutController {

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private UserService userService;

    @PostMapping("/log")
    public ResponseEntity<?> logWorkout(@RequestBody Map<String, Object> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            WorkoutLog workout = new WorkoutLog();
            workout.setUserEmail(email);
            workout.setWorkoutType((String) request.get("workoutType"));
            workout.setExerciseName((String) request.get("exerciseName"));

            if (request.get("sets") != null) {
                workout.setSets(Integer.parseInt(request.get("sets").toString()));
            }
            if (request.get("reps") != null) {
                workout.setReps(Integer.parseInt(request.get("reps").toString()));
            }
            if (request.get("weight") != null) {
                workout.setWeight(Double.parseDouble(request.get("weight").toString()));
            }
            if (request.get("durationMinutes") != null) {
                workout.setDurationMinutes(Integer.parseInt(request.get("durationMinutes").toString()));
            }
            if (request.get("caloriesBurned") != null) {
                workout.setCaloriesBurned(Integer.parseInt(request.get("caloriesBurned").toString()));
            }
            workout.setNotes((String) request.get("notes"));

            WorkoutLog saved = workoutRepository.save(workout);

            // ========== ADD THIS LINE - Update streak ==========
            userService.updateStreak(email, true);
            // ==================================================

            // Check and award achievements after logging workout
            List<Achievement> newAchievements = userService.checkAndAwardAchievements(email);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Workout logged successfully");
            response.put("data", saved);

            if (!newAchievements.isEmpty()) {
                response.put("newAchievements", newAchievements);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getWorkoutHistory() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            List<WorkoutLog> workouts = workoutRepository.findByUserEmailOrderByDateDesc(email);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", workouts);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getWorkoutStats() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            LocalDateTime weekAgo = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
            long workoutsThisWeek = workoutRepository.countByUserEmailAndDateAfter(email, weekAgo);
            long totalWorkouts = workoutRepository.countByUserEmail(email);

            List<WorkoutLog> recentWorkouts = workoutRepository.findTop5ByUserEmailOrderByDateDesc(email);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalWorkouts", totalWorkouts);
            stats.put("workoutsThisWeek", workoutsThisWeek);

            if (!recentWorkouts.isEmpty()) {
                int totalMinutes = recentWorkouts.stream()
                        .mapToInt(w -> w.getDurationMinutes() != null ? w.getDurationMinutes() : 0)
                        .sum();
                stats.put("recentTotalMinutes", totalMinutes);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            response.put("recent", recentWorkouts);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}