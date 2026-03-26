package com.fitness.fitnessplanner.controller;

import com.fitness.fitnessplanner.model.Achievement;
import com.fitness.fitnessplanner.model.Progress;
import com.fitness.fitnessplanner.repository.ProgressRepository;
import com.fitness.fitnessplanner.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/progress")
@CrossOrigin(origins = "*")
public class ProgressController {

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public ResponseEntity<?> addProgress(@RequestBody Map<String, Object> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            Progress progress = new Progress();
            progress.setUserEmail(email);
            progress.setWeight(Double.parseDouble(request.get("weight").toString()));
            progress.setDate(LocalDate.now());

            if (request.get("bodyFat") != null) {
                progress.setBodyFat(Double.parseDouble(request.get("bodyFat").toString()));
            }
            if (request.get("muscleMass") != null) {
                progress.setMuscleMass(Double.parseDouble(request.get("muscleMass").toString()));
            }
            progress.setNotes(request.get("notes") != null ? request.get("notes").toString() : "");

            Progress saved = progressRepository.save(progress);

            // Check and award achievements after logging weight
            List<Achievement> newAchievements = userService.checkAndAwardAchievements(email);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Progress saved successfully");
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

    @GetMapping("/chart")
    public ResponseEntity<?> getChartData() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            List<Progress> progressList = progressRepository.findByUserEmailOrderByDateAsc(email);

            List<Map<String, Object>> chartData = new ArrayList<>();
            for (Progress p : progressList) {
                Map<String, Object> point = new HashMap<>();
                point.put("date", p.getDate().toString());
                point.put("weight", p.getWeight());
                chartData.add(point);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", chartData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getProgressStats() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            List<Progress> progressList = progressRepository.findByUserEmailOrderByDateAsc(email);

            Map<String, Object> stats = new HashMap<>();

            if (progressList.isEmpty()) {
                stats.put("totalEntries", 0);
                stats.put("weightChange", 0);
                stats.put("startWeight", 0);
                stats.put("currentWeight", 0);
            } else {
                Progress first = progressList.get(0);
                Progress last = progressList.get(progressList.size() - 1);

                stats.put("totalEntries", progressList.size());
                stats.put("weightChange", Math.round((last.getWeight() - first.getWeight()) * 10) / 10.0);
                stats.put("startWeight", first.getWeight());
                stats.put("currentWeight", last.getWeight());
                stats.put("averageWeight", Math.round(progressList.stream().mapToDouble(Progress::getWeight).average().orElse(0) * 10) / 10.0);
            }

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