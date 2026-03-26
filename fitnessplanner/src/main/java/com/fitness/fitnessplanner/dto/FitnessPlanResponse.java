package com.fitness.fitnessplanner.dto;

import lombok.Data;
import java.util.List;

@Data
public class FitnessPlanResponse {
    private double bmr;
    private double dailyCalories;
    private double targetCalories;
    private String weightGoal;
    private String goalDescription;
    private DietPlan dietPlan;
    private ExercisePlan exercisePlan;
    private List<String> recommendations;
    private NutritionInfo nutritionInfo;

    public FitnessPlanResponse(double bmr, double dailyCalories, double targetCalories,
                               String weightGoal, String goalDescription, DietPlan dietPlan,
                               ExercisePlan exercisePlan, List<String> recommendations,
                               NutritionInfo nutritionInfo) {
        this.bmr = bmr;
        this.dailyCalories = dailyCalories;
        this.targetCalories = targetCalories;
        this.weightGoal = weightGoal;
        this.goalDescription = goalDescription;
        this.dietPlan = dietPlan;
        this.exercisePlan = exercisePlan;
        this.recommendations = recommendations;
        this.nutritionInfo = nutritionInfo;
    }

    @Data
    public static class DietPlan {
        private String breakfast;
        private String lunch;
        private String dinner;
        private String snacks;
        private String description;
        private List<String> tips;
    }

    @Data
    public static class ExercisePlan {
        private String cardio;
        private String strength;
        private String frequency;
        private String duration;
        private String intensity;
    }

    @Data
    public static class NutritionInfo {
        private double protein;
        private double carbs;
        private double fats;
        private double fiber;
        private String proteinSources;
        private String carbSources;
        private String fatSources;
    }
}