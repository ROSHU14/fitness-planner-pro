package com.fitness.fitnessplanner.controller;

import com.fitness.fitnessplanner.model.Progress;
import com.fitness.fitnessplanner.model.User;
import com.fitness.fitnessplanner.model.MealLog;
import com.fitness.fitnessplanner.model.SleepLog;
import com.fitness.fitnessplanner.model.StepLog;
import com.fitness.fitnessplanner.model.WorkoutLog;
import com.fitness.fitnessplanner.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private MealLogRepository mealLogRepository;

    @Autowired
    private SleepLogRepository sleepRepository;

    @Autowired
    private StepLogRepository stepRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/weekly")
    public ResponseEntity<?> getWeeklyReport() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            LocalDate today = LocalDate.now();
            LocalDate weekAgo = today.minusDays(7);

            Map<String, Object> report = new HashMap<>();
            report.put("period", weekAgo.toString() + " to " + today.toString());

            List<Progress> progressList = progressRepository.findByUserEmailOrderByDateAsc(email);
            if (!progressList.isEmpty()) {
                Progress first = progressList.get(0);
                Progress last = progressList.get(progressList.size() - 1);
                report.put("weightChange", last.getWeight() - first.getWeight());
                report.put("startWeight", first.getWeight());
                report.put("currentWeight", last.getWeight());
            }

            long workoutsThisWeek = workoutRepository.countByUserEmailAndDateAfter(email, weekAgo.atStartOfDay());
            report.put("workoutsThisWeek", workoutsThisWeek);

            List<MealLog> mealsThisWeek = mealLogRepository.findByUserEmailAndDateBetween(email, weekAgo, today);
            int totalCalories = mealsThisWeek.stream().mapToInt(m -> m.getCalories() != null ? m.getCalories() : 0).sum();
            report.put("totalCaloriesThisWeek", totalCalories);

            List<SleepLog> sleepLogs = sleepRepository.findByUserEmailOrderByDateDesc(email);
            double avgSleep = sleepLogs.stream()
                    .limit(7)
                    .mapToDouble(s -> s.getHours() != null ? s.getHours() : 0)
                    .average()
                    .orElse(0);
            report.put("averageSleepHours", Math.round(avgSleep * 10) / 10.0);

            List<StepLog> stepsThisWeek = stepRepository.findByUserEmailOrderByDateDesc(email);
            int totalSteps = stepsThisWeek.stream()
                    .limit(7)
                    .mapToInt(s -> s.getSteps() != null ? s.getSteps() : 0)
                    .sum();
            report.put("totalStepsThisWeek", totalSteps);

            String message;
            if (workoutsThisWeek >= 5) {
                message = "Excellent! You're crushing your fitness goals! 🔥";
            } else if (workoutsThisWeek >= 3) {
                message = "Good job! Stay consistent for best results! 💪";
            } else if (workoutsThisWeek > 0) {
                message = "Great start! Try to increase frequency for better results! 🌟";
            } else {
                message = "Start logging workouts to see progress! Every journey begins with a single step! 🏃‍♂️";
            }
            report.put("motivationalMessage", message);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", report);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportReport(@RequestParam(required = false) String token) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            User user = userRepository.findByEmail(email).orElse(null);
            List<Progress> progressList = progressRepository.findByUserEmailOrderByDateAsc(email);
            List<WorkoutLog> allWorkouts = workoutRepository.findByUserEmailOrderByDateDesc(email);
            long workoutCount = allWorkouts.size();
            long workoutsThisWeek = workoutRepository.countByUserEmailAndDateAfter(email, LocalDate.now().minusDays(7).atStartOfDay());

            List<MealLog> mealsThisWeek = mealLogRepository.findByUserEmailAndDateBetween(email, LocalDate.now().minusDays(7), LocalDate.now());
            int totalCalories = mealsThisWeek.stream().mapToInt(m -> m.getCalories() != null ? m.getCalories() : 0).sum();

            List<SleepLog> sleepLogs = sleepRepository.findByUserEmailOrderByDateDesc(email);
            double avgSleep = sleepLogs.stream()
                    .limit(7)
                    .mapToDouble(s -> s.getHours() != null ? s.getHours() : 0)
                    .average()
                    .orElse(0);

            List<StepLog> stepsThisWeek = stepRepository.findByUserEmailOrderByDateDesc(email);
            int totalSteps = stepsThisWeek.stream()
                    .limit(7)
                    .mapToInt(s -> s.getSteps() != null ? s.getSteps() : 0)
                    .sum();

            // Calculate BMI
            double bmi = 0;
            String bmiCategory = "Normal";
            if (user != null) {
                double heightInMeters = user.getHeight() / 100;
                bmi = user.getWeight() / (heightInMeters * heightInMeters);
                if (bmi < 18.5) bmiCategory = "Underweight";
                else if (bmi < 25) bmiCategory = "Normal";
                else if (bmi < 30) bmiCategory = "Overweight";
                else bmiCategory = "Obese";
            }

            String html = generateProfessionalReportHtml(user, progressList, allWorkouts, workoutCount, workoutsThisWeek,
                    totalCalories, avgSleep, totalSteps, bmi, bmiCategory);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);

            return ResponseEntity.ok().headers(headers).body(html);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error generating report: " + e.getMessage());
        }
    }

    private String generateProfessionalReportHtml(User user, List<Progress> progressList, List<WorkoutLog> allWorkouts,
                                                  long workoutCount, long workoutsThisWeek, int totalCalories,
                                                  double avgSleep, int totalSteps, double bmi, String bmiCategory) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html lang='en'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>Fitness Progress Report</title>");
        html.append("<style>");
        html.append("* { margin: 0; padding: 0; box-sizing: border-box; }");
        html.append("body { font-family: 'Segoe UI', 'Inter', Arial, sans-serif; background: #f0f2f5; padding: 40px; }");
        html.append(".report-container { max-width: 1200px; margin: 0 auto; background: white; border-radius: 20px; box-shadow: 0 20px 40px rgba(0,0,0,0.1); overflow: hidden; }");
        html.append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 40px; text-align: center; color: white; }");
        html.append(".header h1 { font-size: 32px; margin-bottom: 10px; }");
        html.append(".header .date { margin-top: 15px; font-size: 12px; opacity: 0.8; }");
        html.append(".content { padding: 40px; }");

        // User Card
        html.append(".user-card { background: linear-gradient(135deg, #f8f9ff 0%, #f0f2ff 100%); border-radius: 16px; padding: 25px; margin-bottom: 30px; border-left: 4px solid #667eea; }");
        html.append(".user-card h2 { color: #333; font-size: 20px; margin-bottom: 15px; }");
        html.append(".user-info-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; margin-top: 15px; }");
        html.append(".user-info-item { display: flex; flex-direction: column; }");
        html.append(".user-info-label { font-size: 12px; color: #666; text-transform: uppercase; }");
        html.append(".user-info-value { font-size: 18px; font-weight: 600; color: #333; margin-top: 5px; }");

        // Stats Grid
        html.append(".stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 30px; }");
        html.append(".stat-card { background: white; border: 1px solid #e5e7eb; border-radius: 16px; padding: 20px; text-align: center; }");
        html.append(".stat-icon { font-size: 32px; margin-bottom: 10px; }");
        html.append(".stat-value { font-size: 28px; font-weight: bold; color: #667eea; }");
        html.append(".stat-label { font-size: 12px; color: #666; margin-top: 5px; }");

        // Tables
        html.append(".section-title { font-size: 18px; font-weight: 600; color: #333; margin: 30px 0 20px 0; padding-bottom: 10px; border-bottom: 2px solid #667eea; display: inline-block; }");
        html.append("table { width: 100%; border-collapse: collapse; margin-bottom: 30px; }");
        html.append("th { background: #f8f9fa; padding: 12px; text-align: left; font-weight: 600; color: #333; border-bottom: 2px solid #e5e7eb; }");
        html.append("td { padding: 12px; border-bottom: 1px solid #f0f2f5; color: #555; }");
        html.append(".progress-summary { background: #f8f9fa; border-radius: 16px; padding: 20px; margin: 30px 0; }");
        html.append(".progress-bar-container { background: #e5e7eb; border-radius: 10px; height: 10px; overflow: hidden; margin: 15px 0; }");
        html.append(".progress-bar { background: linear-gradient(90deg, #667eea, #764ba2); height: 100%; width: 0%; border-radius: 10px; }");
        html.append(".footer { background: #f8f9fa; padding: 20px 40px; text-align: center; border-top: 1px solid #e5e7eb; }");
        html.append("@media print { body { padding: 0; background: white; } .report-container { box-shadow: none; } }");
        html.append("</style>");
        html.append("</head><body>");

        html.append("<div class='report-container'>");
        html.append("<div class='header'>");
        html.append("<h1>💪 Fitness Planner Pro</h1>");
        html.append("<h2>Progress Report</h2>");
        html.append("<div class='date'>Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) + "</div>");
        html.append("</div>");

        html.append("<div class='content'>");

        // User Information
        if (user != null) {
            html.append("<div class='user-card'>");
            html.append("<h2>👤 User Profile</h2>");
            html.append("<div class='user-info-grid'>");
            html.append("<div class='user-info-item'><span class='user-info-label'>Name</span><span class='user-info-value'>" + user.getName() + "</span></div>");
            html.append("<div class='user-info-item'><span class='user-info-label'>Email</span><span class='user-info-value'>" + user.getEmail() + "</span></div>");
            html.append("<div class='user-info-item'><span class='user-info-label'>Age / Gender</span><span class='user-info-value'>" + user.getAge() + " / " + user.getGender() + "</span></div>");
            html.append("<div class='user-info-item'><span class='user-info-label'>Height / Weight</span><span class='user-info-value'>" + user.getHeight() + " cm / " + user.getWeight() + " kg</span></div>");
            html.append("<div class='user-info-item'><span class='user-info-label'>BMI</span><span class='user-info-value'>" + Math.round(bmi * 10) / 10.0 + " (" + bmiCategory + ")</span></div>");
            html.append("<div class='user-info-item'><span class='user-info-label'>Fitness Goal</span><span class='user-info-value'>" + user.getWeightGoal() + "</span></div>");
            html.append("</div></div>");
        }

        // Stats Grid
        html.append("<div class='stats-grid'>");
        html.append("<div class='stat-card'><div class='stat-icon'>🏋️</div><div class='stat-value'>" + workoutCount + "</div><div class='stat-label'>Total Workouts</div></div>");
        html.append("<div class='stat-card'><div class='stat-icon'>⚡</div><div class='stat-value'>" + workoutsThisWeek + "</div><div class='stat-label'>This Week</div></div>");
        html.append("<div class='stat-card'><div class='stat-icon'>🍽️</div><div class='stat-value'>" + totalCalories + "</div><div class='stat-label'>Calories (Week)</div></div>");
        html.append("<div class='stat-card'><div class='stat-icon'>👣</div><div class='stat-value'>" + totalSteps + "</div><div class='stat-label'>Steps (Week)</div></div>");
        html.append("<div class='stat-card'><div class='stat-icon'>😴</div><div class='stat-value'>" + avgSleep + "</div><div class='stat-label'>Avg Sleep (hrs)</div></div>");
        html.append("</div>");

        // Workout History Table
        if (!allWorkouts.isEmpty()) {
            html.append("<h3 class='section-title'>🏋️ Recent Workouts</h3>");
            html.append("<table>");
            html.append("<thead><tr><th>Date</th><th>Exercise</th><th>Type</th><th>Duration</th><th>Calories</th></tr></thead>");
            html.append("<tbody>");
            int count = 0;
            for (int i = allWorkouts.size() - 1; i >= 0 && count < 10; i--, count++) {
                WorkoutLog w = allWorkouts.get(i);
                html.append("<tr>");
                html.append("<td>" + (w.getDate() != null ? w.getDate().toLocalDate().toString() : "N/A") + "</td>");
                html.append("<td>" + (w.getExerciseName() != null ? w.getExerciseName() : "Workout") + "</td>");
                html.append("<td>" + (w.getWorkoutType() != null ? w.getWorkoutType() : "General") + "</td>");
                html.append("<td>" + (w.getDurationMinutes() != null ? w.getDurationMinutes() : 0) + " min</td>");
                html.append("<td>" + (w.getCaloriesBurned() != null ? w.getCaloriesBurned() : "—") + "</td>");
                html.append("</tr>");
            }
            html.append("</tbody></table>");
        } else {
            html.append("<div class='progress-summary'><p>📝 No workouts logged yet. Start logging to see your progress!</p></div>");
        }

        // Weight Progress Table
        if (!progressList.isEmpty()) {
            html.append("<h3 class='section-title'>📊 Weight Progress</h3>");
            html.append("<table>");
            html.append("<thead><tr><th>Date</th><th>Weight (kg)</th><th>Change</th></tr></thead>");
            html.append("<tbody>");
            for (int i = progressList.size() - 1; i >= 0 && i >= progressList.size() - 10; i--) {
                Progress p = progressList.get(i);
                String change = "";
                if (i > 0) {
                    double diff = p.getWeight() - progressList.get(i - 1).getWeight();
                    if (diff > 0) change = "▲ +" + String.format("%.1f", diff);
                    else if (diff < 0) change = "▼ " + String.format("%.1f", diff);
                    else change = "● 0";
                }
                html.append("<tr><td>" + p.getDate() + "</td><td><strong>" + p.getWeight() + " kg</strong></td><td>" + change + "</td></tr>");
            }
            html.append("</tbody></table>");

            // Progress Summary
            Progress first = progressList.get(0);
            Progress last = progressList.get(progressList.size() - 1);
            double totalChange = last.getWeight() - first.getWeight();
            double percentChange = (Math.abs(totalChange) / first.getWeight()) * 100;
            String trend = totalChange < 0 ? "lost" : (totalChange > 0 ? "gained" : "maintained");
            html.append("<div class='progress-summary'>");
            html.append("<p>Starting: <strong>" + first.getWeight() + " kg</strong> → Current: <strong>" + last.getWeight() + " kg</strong></p>");
            html.append("<p>You have <strong>" + trend + " " + Math.abs(totalChange) + " kg</strong> (" + Math.round(percentChange) + "%)</p>");
            html.append("<div class='progress-bar-container'><div class='progress-bar' style='width: " + Math.min(100, percentChange) + "%;'></div></div>");
            html.append("</div>");
        }

        // Health Metrics
        html.append("<h3 class='section-title'>❤️ Health Metrics</h3>");
        html.append("<table><thead><tr><th>Metric</th><th>Value</th><th>Status</th><th>Recommendation</th></tr></thead><tbody>");

        String bmiStatus = bmi < 18.5 ? "⚠️ Underweight" : (bmi < 25 ? "✅ Healthy" : (bmi < 30 ? "⚠️ Overweight" : "⚠️ Obese"));
        String bmiRec = bmi < 18.5 ? "Increase calorie intake with nutrient-dense foods" : (bmi < 25 ? "Maintain healthy habits" : (bmi < 30 ? "Focus on portion control" : "Consult a healthcare provider"));
        html.append("<tr><td>BMI</td><td>" + Math.round(bmi * 10) / 10.0 + "</td><td>" + bmiStatus + "</td><td>" + bmiRec + "</td></tr>");

        String sleepStatus = avgSleep >= 7 ? "✅ Good" : (avgSleep >= 6 ? "⚠️ Fair" : "⚠️ Poor");
        html.append("<tr><td>Average Sleep</td><td>" + avgSleep + " hrs</td><td>" + sleepStatus + "</td><td>Aim for 7-9 hours</td></tr>");

        double avgSteps = totalSteps / 7.0;
        String stepsStatus = avgSteps >= 10000 ? "✅ Excellent" : (avgSteps >= 5000 ? "⚠️ Good" : "⚠️ Low");
        html.append("<tr><td>Daily Steps</td><td>" + Math.round(avgSteps) + "</td><td>" + stepsStatus + "</td><td>Try to reach 10,000 steps</td></tr>");

        html.append("</tbody></table>");

        // Motivational Quote
        String[] quotes = {"🔥 \"The only bad workout is the one that didn't happen.\"", "💪 \"Your only limit is you.\"", "🎯 \"Progress, not perfection.\"", "⚡ \"Believe you can and you're halfway there.\""};
        html.append("<div class='progress-summary' style='text-align: center;'><p style='font-size: 16px; font-style: italic;'>✨ " + quotes[(int)(Math.random() * quotes.length)] + " ✨</p></div>");

        html.append("</div>");
        html.append("<div class='footer'><p>Fitness Planner Pro - Stay consistent, stay motivated! 💪</p></div>");
        html.append("</div></body></html>");

        return html.toString();
    }
}