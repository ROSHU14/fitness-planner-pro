package com.fitness.fitnessplanner.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.util.Map;

@Document(collection = "weekly_meal_plans")
public class WeeklyMealPlan {

    @Id
    private String id;

    private String userEmail;

    private LocalDate weekStartDate;

    private Map<String, DailyMeals> weekPlan;

    private Integer totalCalories;

    public static class DailyMeals {
        private String breakfast;
        private String lunch;
        private String dinner;
        private String snacks;
        private Integer calories;

        public String getBreakfast() { return breakfast; }
        public void setBreakfast(String breakfast) { this.breakfast = breakfast; }

        public String getLunch() { return lunch; }
        public void setLunch(String lunch) { this.lunch = lunch; }

        public String getDinner() { return dinner; }
        public void setDinner(String dinner) { this.dinner = dinner; }

        public String getSnacks() { return snacks; }
        public void setSnacks(String snacks) { this.snacks = snacks; }

        public Integer getCalories() { return calories; }
        public void setCalories(Integer calories) { this.calories = calories; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public LocalDate getWeekStartDate() { return weekStartDate; }
    public void setWeekStartDate(LocalDate weekStartDate) { this.weekStartDate = weekStartDate; }

    public Map<String, DailyMeals> getWeekPlan() { return weekPlan; }
    public void setWeekPlan(Map<String, DailyMeals> weekPlan) { this.weekPlan = weekPlan; }

    public Integer getTotalCalories() { return totalCalories; }
    public void setTotalCalories(Integer totalCalories) { this.totalCalories = totalCalories; }
}