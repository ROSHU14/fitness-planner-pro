package com.fitness.fitnessplanner.controller;

import com.fitness.fitnessplanner.dto.LoginRequest;
import com.fitness.fitnessplanner.dto.SignupRequest;
import com.fitness.fitnessplanner.model.User;
import com.fitness.fitnessplanner.security.JwtService;
import com.fitness.fitnessplanner.service.EmailService;
import com.fitness.fitnessplanner.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    // ========== ADD THIS - Inject EmailService ==========
    @Autowired
    private EmailService emailService;
    // ====================================================

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        try {
            User user = userService.registerUser(request);

            // ========== ADD THIS - Send Welcome Email ==========
            try {
                emailService.sendWelcomeEmail(user);
                System.out.println("✅ Welcome email sent to: " + user.getEmail());
            } catch (Exception e) {
                System.err.println("❌ Failed to send welcome email: " + e.getMessage());
                // Don't fail signup if email fails
            }
            // ==================================================

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User registered successfully. Welcome email sent!");
            response.put("userId", user.getId());
            response.put("email", user.getEmail());
            response.put("name", user.getName());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", token);
            response.put("message", "Login successful");
            response.put("email", userDetails.getUsername());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Invalid email or password");
            return ResponseEntity.status(401).body(error);
        }
    }
}