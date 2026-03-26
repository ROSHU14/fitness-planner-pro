package com.fitness.fitnessplanner.controller;

import com.fitness.fitnessplanner.model.SleepLog;
import com.fitness.fitnessplanner.repository.SleepLogRepository;
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
@RequestMapping("/api/sleep")
@CrossOrigin(origins = "*")
public class SleepController {

    @Autowired
    private SleepLogRepository sleepRepository;

    @PostMapping("/log")
    public ResponseEntity<?> logSleep(@RequestBody Map<String, Object> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            SleepLog sleep = new SleepLog();
            sleep.setUserEmail(email);
            sleep.setDate(LocalDate.now());
            sleep.setHours(Double.parseDouble(request.get("hours").toString()));
            sleep.setQuality((String) request.get("quality"));

            SleepLog saved = sleepRepository.save(sleep);

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

    @GetMapping("/stats")
    public ResponseEntity<?> getSleepStats() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            List<SleepLog> sleepLogs = sleepRepository.findByUserEmailOrderByDateDesc(email);
            double avgHours = sleepLogs.stream().limit(7).mapToDouble(SleepLog::getHours).average().orElse(0);

            Map<String, Object> stats = new HashMap<>();
            stats.put("averageHours", Math.round(avgHours * 10) / 10.0);

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