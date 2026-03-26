package com.fitness.fitnessplanner.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

@Document(collection = "measurements")
public class BodyMeasurement {

    @Id
    private String id;

    private String userEmail;

    private LocalDate date;

    private Double chest;

    private Double waist;

    private Double hips;

    private Double arms;

    private Double thighs;

    private Double neck;

    public BodyMeasurement() {
        this.date = LocalDate.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Double getChest() { return chest; }
    public void setChest(Double chest) { this.chest = chest; }

    public Double getWaist() { return waist; }
    public void setWaist(Double waist) { this.waist = waist; }

    public Double getHips() { return hips; }
    public void setHips(Double hips) { this.hips = hips; }

    public Double getArms() { return arms; }
    public void setArms(Double arms) { this.arms = arms; }

    public Double getThighs() { return thighs; }
    public void setThighs(Double thighs) { this.thighs = thighs; }

    public Double getNeck() { return neck; }
    public void setNeck(Double neck) { this.neck = neck; }
}