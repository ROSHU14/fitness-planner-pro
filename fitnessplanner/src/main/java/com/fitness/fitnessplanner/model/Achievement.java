package com.fitness.fitnessplanner.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "achievements")
public class Achievement {

    @Id
    private String id;

    private String userEmail;

    private String badgeName;

    private String description;

    private String icon;

    private LocalDateTime earnedDate;

    public Achievement() {
        this.earnedDate = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getBadgeName() { return badgeName; }
    public void setBadgeName(String badgeName) { this.badgeName = badgeName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public LocalDateTime getEarnedDate() { return earnedDate; }
    public void setEarnedDate(LocalDateTime earnedDate) { this.earnedDate = earnedDate; }
}