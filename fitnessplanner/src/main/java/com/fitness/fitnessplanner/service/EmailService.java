package com.fitness.fitnessplanner.service;

import com.fitness.fitnessplanner.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendWelcomeEmail(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Welcome to Fitness Planner Pro! 💪");
            message.setText(String.format(
                    "Hi %s,\n\n" +
                            "Welcome to Fitness Planner Pro! 🎉\n\n" +
                            "We're excited to help you on your fitness journey.\n\n" +
                            "Your Profile:\n" +
                            "• Height: %.1f cm\n" +
                            "• Weight: %.1f kg\n" +
                            "• Goal: %s\n\n" +
                            "Get started by:\n" +
                            "1. Logging your first workout\n" +
                            "2. Tracking your meals\n" +
                            "3. Checking your personalized fitness plan\n\n" +
                            "Stay consistent! 💪\n\n" +
                            "Best regards,\n" +
                            "Fitness Planner Pro Team",
                    user.getName(),
                    user.getHeight(),
                    user.getWeight(),
                    user.getWeightGoal()
            ));

            mailSender.send(message);
            System.out.println("Email sent successfully to: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            throw new RuntimeException("Email sending failed: " + e.getMessage());
        }
    }
}