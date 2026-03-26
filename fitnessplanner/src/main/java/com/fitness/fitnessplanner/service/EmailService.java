package com.fitness.fitnessplanner.service;

import com.fitness.fitnessplanner.model.User;
import com.fitness.fitnessplanner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    // Send weekly reports every Monday at 9 AM
    @Scheduled(cron = "0 0 9 * * MON")
    public void sendWeeklyReports() {
        System.out.println("📧 Starting weekly email reports...");
        List<User> users = userRepository.findAll();
        System.out.println("📧 Found " + users.size() + " users to send emails to");

        for (User user : users) {
            try {
                sendWeeklyReport(user);
                System.out.println("✅ Weekly report sent to: " + user.getEmail());
            } catch (Exception e) {
                System.err.println("❌ Failed to send email to " + user.getEmail() + ": " + e.getMessage());
            }
        }
        System.out.println("📧 Weekly email reports completed");
    }

    public void sendWeeklyReport(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("📊 Your Weekly Fitness Report - " +
                LocalDate.now().minusDays(7).format(DateTimeFormatter.ofPattern("dd MMM")) +
                " to " +
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));

        String emailContent = generateWeeklyReportContent(user);
        message.setText(emailContent);

        mailSender.send(message);
        System.out.println("📧 Weekly report email sent to: " + user.getEmail());
    }

    public void sendWelcomeEmail(User user) {
        System.out.println("📧 Preparing welcome email for: " + user.getEmail());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Welcome to Fitness Planner Pro! 💪");
        message.setText("Hi " + user.getName() + ",\n\n" +
                "🎉 Welcome to Fitness Planner Pro! 🎉\n\n" +
                "We're excited to help you on your fitness journey.\n\n" +
                "✨ Here's what you can do:\n" +
                "✅ Log your workouts and track progress\n" +
                "✅ Monitor your daily meals and calories\n" +
                "✅ Track water intake, sleep, and steps\n" +
                "✅ Get personalized diet and exercise plans\n" +
                "✅ Earn achievements and maintain streaks\n\n" +
                "🔐 Login here: http://localhost:8080/login.html\n\n" +
                "💡 Pro Tip: Log your first workout today to start your streak!\n\n" +
                "Stay consistent and you'll see amazing results!\n\n" +
                "Best regards,\n" +
                "Fitness Planner Pro Team 💪");

        mailSender.send(message);
        System.out.println("✅ Welcome email sent successfully to: " + user.getEmail());
    }

    public void sendMotivationalEmail(User user) {
        String[] quotes = {
                "💪 \"The only bad workout is the one that didn't happen.\"",
                "🔥 \"Your only limit is you.\"",
                "🏃‍♂️ \"Success starts with self-discipline.\"",
                "🌟 \"Small steps every day add up to big results.\"",
                "💯 \"Don't stop when you're tired. Stop when you're done.\"",
                "🎯 \"Progress, not perfection.\"",
                "⚡ \"Believe you can and you're halfway there.\""
        };

        String randomQuote = quotes[(int)(Math.random() * quotes.length)];

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("💪 Daily Motivation from Fitness Planner Pro");
        message.setText("Hi " + user.getName() + ",\n\n" +
                randomQuote + "\n\n" +
                "Remember: Every workout brings you one step closer to your goal!\n\n" +
                "Log in today and make progress: http://localhost:8080/login.html\n\n" +
                "Stay strong! 💪\n\n" +
                "Fitness Planner Pro Team");

        mailSender.send(message);
        System.out.println("✅ Motivational email sent to: " + user.getEmail());
    }

    private String generateWeeklyReportContent(User user) {
        try {
            Map<String, Object> stats = userService.getProgressStats(user.getEmail());
            Map<String, Object> streak = userService.getUserStreak(user.getEmail());

            double weightChange = (double) stats.get("weightChange");
            String changeEmoji = weightChange < 0 ? "🎉" : (weightChange > 0 ? "📈" : "⚖️");
            String changeText = weightChange < 0 ? "Lost " + Math.abs(weightChange) + " kg" :
                    (weightChange > 0 ? "Gained " + weightChange + " kg" : "Maintained weight");

            return "Hi " + user.getName() + ",\n\n" +
                    "📊 Here's your weekly fitness summary:\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                    "⚖️ WEIGHT PROGRESS:\n" +
                    "• Starting Weight: " + stats.get("startWeight") + " kg\n" +
                    "• Current Weight: " + stats.get("currentWeight") + " kg\n" +
                    "• Change: " + changeText + " " + changeEmoji + "\n\n" +
                    "🏋️ WORKOUT STATS:\n" +
                    "• Total Workouts: " + stats.get("totalEntries") + "\n\n" +
                    "🔥 STREAK:\n" +
                    "• Current Streak: " + streak.get("currentStreak") + " days\n" +
                    "• Longest Streak: " + streak.get("longestStreak") + " days\n\n" +
                    "💪 Keep up the great work! Every day counts.\n\n" +
                    "🔐 View your full report: http://localhost:8080/login.html\n\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "Best regards,\n" +
                    "Fitness Planner Pro Team 💪";
        } catch (Exception e) {
            // Fallback if stats not available
            return "Hi " + user.getName() + ",\n\n" +
                    "📊 Here's your weekly fitness summary:\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                    "Keep logging your workouts and meals to see detailed stats!\n\n" +
                    "💪 Every workout brings you closer to your goal!\n\n" +
                    "🔐 Login here: http://localhost:8080/login.html\n\n" +
                    "Best regards,\n" +
                    "Fitness Planner Pro Team 💪";
        }
    }
}