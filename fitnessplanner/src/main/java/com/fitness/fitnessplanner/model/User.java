package com.fitness.fitnessplanner.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String password;

    private String name;

    private Double height;

    private Double weight;

    private String workoutFrequency;

    private String weightGoal; // "LOSS", "GAIN", "MAINTAIN"

    private String gender;

    private Integer age;

    private LocalDateTime createdAt;

    public User() {
        this.createdAt = LocalDateTime.now();
        this.weightGoal = "LOSS"; // default
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public String getWorkoutFrequency() { return workoutFrequency; }
    public void setWorkoutFrequency(String workoutFrequency) { this.workoutFrequency = workoutFrequency; }

    public String getWeightGoal() { return weightGoal; }
    public void setWeightGoal(String weightGoal) { this.weightGoal = weightGoal; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}