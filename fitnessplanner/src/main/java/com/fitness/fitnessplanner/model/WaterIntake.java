package com.fitness.fitnessplanner.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "water")
public class WaterIntake {

    @Id
    private String id;

    private String userEmail;

    private LocalDate date;

    private Integer cups;

    private Double liters;

    private LocalDateTime lastUpdated;

    public WaterIntake() {
        this.date = LocalDate.now();
        this.lastUpdated = LocalDateTime.now();
        this.cups = 0;
        this.liters = 0.0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Integer getCups() { return cups; }
    public void setCups(Integer cups) { this.cups = cups; }

    public Double getLiters() { return liters; }
    public void setLiters(Double liters) { this.liters = liters; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}