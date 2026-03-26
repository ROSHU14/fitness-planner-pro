package com.fitness.fitnessplanner.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SignupRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Height is required")
    @Positive(message = "Height must be positive")
    private Double height;

    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    private Double weight;

    @NotBlank(message = "Workout frequency is required")
    private String workoutFrequency;

    private String weightGoal; // "LOSS", "GAIN", "MAINTAIN"

    private String gender;

    @Min(value = 10, message = "Age must be at least 10")
    @Max(value = 120, message = "Age must be less than 120")
    private Integer age;
}