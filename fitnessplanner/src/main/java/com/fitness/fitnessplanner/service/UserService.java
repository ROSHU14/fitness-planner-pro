package com.fitness.fitnessplanner.service;

import com.fitness.fitnessplanner.dto.FitnessPlanResponse;
import com.fitness.fitnessplanner.dto.SignupRequest;
import com.fitness.fitnessplanner.model.Achievement;
import com.fitness.fitnessplanner.model.Progress;
import com.fitness.fitnessplanner.model.User;
import com.fitness.fitnessplanner.model.UserStreak;
import com.fitness.fitnessplanner.model.WorkoutLog;
import com.fitness.fitnessplanner.model.MealLog;
import com.fitness.fitnessplanner.model.SleepLog;
import com.fitness.fitnessplanner.model.StepLog;
import com.fitness.fitnessplanner.model.ProgressPhoto;
import com.fitness.fitnessplanner.model.BodyMeasurement;
import com.fitness.fitnessplanner.repository.AchievementRepository;
import com.fitness.fitnessplanner.repository.ProgressRepository;
import com.fitness.fitnessplanner.repository.UserRepository;
import com.fitness.fitnessplanner.repository.UserStreakRepository;
import com.fitness.fitnessplanner.repository.WorkoutRepository;
import com.fitness.fitnessplanner.repository.MealLogRepository;
import com.fitness.fitnessplanner.repository.SleepLogRepository;
import com.fitness.fitnessplanner.repository.StepLogRepository;
import com.fitness.fitnessplanner.repository.ProgressPhotoRepository;
import com.fitness.fitnessplanner.repository.BodyMeasurementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private UserStreakRepository streakRepository;

    @Autowired
    private MealLogRepository mealLogRepository;

    @Autowired
    private SleepLogRepository sleepLogRepository;

    @Autowired
    private StepLogRepository stepLogRepository;

    @Autowired
    private ProgressPhotoRepository photoRepository;

    @Autowired
    private BodyMeasurementRepository measurementRepository;

    public User registerUser(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered!");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setHeight(request.getHeight());
        user.setWeight(request.getWeight());
        user.setWorkoutFrequency(request.getWorkoutFrequency());
        user.setWeightGoal(request.getWeightGoal() != null ? request.getWeightGoal().toUpperCase() : "LOSS");
        user.setGender(request.getGender() != null ? request.getGender() : "male");
        user.setAge(request.getAge() != null ? request.getAge() : 25);
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public FitnessPlanResponse generateFitnessPlan(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Calculate BMR using Mifflin-St Jeor Equation
        double bmr;
        if ("female".equalsIgnoreCase(user.getGender())) {
            bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() - 161;
        } else {
            bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() + 5;
        }

        // Activity multiplier based on workout frequency
        double activityMultiplier;
        String activityLevel;
        switch (user.getWorkoutFrequency().toLowerCase()) {
            case "sedentary":
                activityMultiplier = 1.2;
                activityLevel = "Sedentary";
                break;
            case "moderate":
                activityMultiplier = 1.375;
                activityLevel = "Moderately Active";
                break;
            case "active":
                activityMultiplier = 1.55;
                activityLevel = "Active";
                break;
            case "very_active":
                activityMultiplier = 1.725;
                activityLevel = "Very Active";
                break;
            default:
                activityMultiplier = 1.2;
                activityLevel = "Sedentary";
        }

        double maintenanceCalories = bmr * activityMultiplier;

        // Calculate target calories based on goal
        double targetCalories;
        String weightGoal;
        String goalDescription;
        int calorieAdjustment;

        switch (user.getWeightGoal().toUpperCase()) {
            case "GAIN":
                calorieAdjustment = 500;
                targetCalories = maintenanceCalories + calorieAdjustment;
                weightGoal = "Weight Gain / Muscle Building";
                goalDescription = "You're in a +" + calorieAdjustment + " calorie surplus. Focus on nutrient-dense foods, eat every 3-4 hours, and prioritize protein with every meal.";
                break;
            case "MAINTAIN":
                calorieAdjustment = 0;
                targetCalories = maintenanceCalories;
                weightGoal = "Weight Maintenance";
                goalDescription = "You're eating at maintenance calories. Focus on balanced nutrition, portion control, and consistent exercise.";
                break;
            case "LOSS":
            default:
                calorieAdjustment = -500;
                targetCalories = maintenanceCalories + calorieAdjustment;
                weightGoal = "Weight Loss";
                goalDescription = "You're in a " + calorieAdjustment + " calorie deficit. Focus on high-protein, fiber-rich foods to stay full and energized.";
                break;
        }

        // Calculate BMI
        double bmi = calculateBMI(user.getWeight(), user.getHeight());
        String bmiCategory = getBMICategory(bmi);

        // Calculate macros based on goal and weight
        double proteinGrams, carbsGrams, fatsGrams;

        if (user.getWeightGoal().equalsIgnoreCase("GAIN")) {
            proteinGrams = user.getWeight() * 2.0;
            carbsGrams = (targetCalories * 0.5) / 4;
            fatsGrams = (targetCalories * 0.25) / 9;
        } else if (user.getWeightGoal().equalsIgnoreCase("MAINTAIN")) {
            proteinGrams = user.getWeight() * 1.6;
            carbsGrams = (targetCalories * 0.45) / 4;
            fatsGrams = (targetCalories * 0.3) / 9;
        } else {
            proteinGrams = user.getWeight() * 1.8;
            carbsGrams = (targetCalories * 0.35) / 4;
            fatsGrams = (targetCalories * 0.3) / 9;
        }

        // DYNAMIC DIET PLAN based on ALL factors
        FitnessPlanResponse.DietPlan dietPlan = generateDynamicDietPlan(
                user, targetCalories, proteinGrams, carbsGrams, fatsGrams, bmiCategory, activityLevel
        );

        // DYNAMIC EXERCISE PLAN based on goal and frequency
        FitnessPlanResponse.ExercisePlan exercisePlan = generateDynamicExercisePlan(
                user.getWorkoutFrequency(), user.getWeightGoal(), user.getWeight()
        );

        // Generate nutrition info
        FitnessPlanResponse.NutritionInfo nutritionInfo = generateNutritionInfo(
                targetCalories, user.getWeightGoal(), user.getWeight(), proteinGrams, carbsGrams, fatsGrams
        );

        // Generate recommendations
        List<String> recommendations = generateDynamicRecommendations(user, targetCalories, bmiCategory);

        return new FitnessPlanResponse(
                bmr,
                maintenanceCalories,
                targetCalories,
                weightGoal,
                goalDescription,
                dietPlan,
                exercisePlan,
                recommendations,
                nutritionInfo
        );
    }

    // ========== DYNAMIC DIET PLAN GENERATOR ==========
    private FitnessPlanResponse.DietPlan generateDynamicDietPlan(User user, double calories,
                                                                 double protein, double carbs, double fats, String bmiCategory, String activityLevel) {

        FitnessPlanResponse.DietPlan dietPlan = new FitnessPlanResponse.DietPlan();
        List<String> tips = new ArrayList<>();

        // Calculate meal distribution based on calorie target
        double breakfastCal = calories * 0.25;
        double lunchCal = calories * 0.35;
        double dinnerCal = calories * 0.30;
        double snackCal = calories * 0.10;

        String breakfast = generateDynamicBreakfast(user, breakfastCal, bmiCategory);
        String lunch = generateDynamicLunch(user, lunchCal, protein);
        String dinner = generateDynamicDinner(user, dinnerCal, activityLevel);
        String snacks = generateDynamicSnacks(user, snackCal);

        dietPlan.setBreakfast(breakfast);
        dietPlan.setLunch(lunch);
        dietPlan.setDinner(dinner);
        dietPlan.setSnacks(snacks);

        String description = generateDynamicDescription(user, bmiCategory, calories);
        dietPlan.setDescription(description);

        tips.addAll(generateDynamicTips(user, bmiCategory));
        dietPlan.setTips(tips);
        return dietPlan;
    }

    private String generateDynamicBreakfast(User user, double calories, String bmiCategory) {
        int cal = (int) Math.round(calories);
        String goal = user.getWeightGoal();

        if (goal.equalsIgnoreCase("GAIN")) {
            if (cal > 700) {
                return "🥑 " + cal + " kcal: 4 scrambled eggs with whole grain toast, avocado, and banana protein smoothie";
            } else if (cal > 500) {
                return "🥑 " + cal + " kcal: 3 eggs with oatmeal, berries, and peanut butter toast";
            } else {
                return "🥑 " + cal + " kcal: 2 eggs with Greek yogurt, granola, and banana";
            }
        } else if (goal.equalsIgnoreCase("LOSS")) {
            if (cal > 500) {
                return "🥣 " + cal + " kcal: Oatmeal with berries, 2 boiled eggs, and green tea";
            } else if (cal > 350) {
                return "🥣 " + cal + " kcal: Greek yogurt with berries and 1 boiled egg";
            } else {
                return "🥣 " + cal + " kcal: 2 eggs with spinach and 1 slice whole grain toast";
            }
        } else {
            if (cal > 600) {
                return "🍳 " + cal + " kcal: 3 scrambled eggs, avocado toast, and mixed berries";
            } else if (cal > 400) {
                return "🍳 " + cal + " kcal: 2 eggs with oatmeal and banana";
            } else {
                return "🍳 " + cal + " kcal: Greek yogurt with honey and granola";
            }
        }
    }

    private String generateDynamicLunch(User user, double calories, double proteinNeeds) {
        int cal = (int) Math.round(calories);
        double protein = Math.round(proteinNeeds / 3);
        String goal = user.getWeightGoal();

        if (goal.equalsIgnoreCase("GAIN")) {
            if (cal > 800) {
                return "🍗 " + cal + " kcal: 250g chicken breast, 1.5 cups brown rice, roasted vegetables (" + (int)protein + "g protein)";
            } else if (cal > 600) {
                return "🍗 " + cal + " kcal: 200g grilled chicken, 1 cup quinoa, mixed vegetables (" + (int)protein + "g protein)";
            } else {
                return "🍗 " + cal + " kcal: 150g chicken, 1 cup rice, vegetables (" + (int)protein + "g protein)";
            }
        } else if (goal.equalsIgnoreCase("LOSS")) {
            if (cal > 600) {
                return "🥗 " + cal + " kcal: 200g grilled chicken, large salad, quinoa (" + (int)protein + "g protein)";
            } else if (cal > 450) {
                return "🥗 " + cal + " kcal: 150g chicken breast, large salad with olive oil (" + (int)protein + "g protein)";
            } else {
                return "🥗 " + cal + " kcal: 120g grilled fish, mixed greens (" + (int)protein + "g protein)";
            }
        } else {
            if (cal > 700) {
                return "🥗 " + cal + " kcal: 200g salmon, quinoa bowl, avocado (" + (int)protein + "g protein)";
            } else if (cal > 500) {
                return "🥗 " + cal + " kcal: 180g chicken, brown rice, mixed vegetables (" + (int)protein + "g protein)";
            } else {
                return "🥗 " + cal + " kcal: 150g turkey, salad, whole grain bread (" + (int)protein + "g protein)";
            }
        }
    }

    private String generateDynamicDinner(User user, double calories, String activityLevel) {
        int cal = (int) Math.round(calories);
        String goal = user.getWeightGoal();

        if (goal.equalsIgnoreCase("GAIN")) {
            if (cal > 700) {
                return "🐟 " + cal + " kcal: 250g salmon, large sweet potato, quinoa, steamed broccoli";
            } else if (cal > 550) {
                return "🐟 " + cal + " kcal: 200g lean beef, sweet potato, green beans";
            } else {
                return "🐟 " + cal + " kcal: 180g fish, roasted vegetables, brown rice";
            }
        } else if (goal.equalsIgnoreCase("LOSS")) {
            if (cal > 550) {
                return "🐟 " + cal + " kcal: 200g baked fish, steamed broccoli, quinoa";
            } else if (cal > 400) {
                return "🐟 " + cal + " kcal: 150g lean fish, large salad, asparagus";
            } else {
                return "🐟 " + cal + " kcal: 120g grilled fish, steamed vegetables, small sweet potato";
            }
        } else {
            if (cal > 650) {
                return "🦃 " + cal + " kcal: 200g turkey breast, sweet potato, roasted vegetables";
            } else if (cal > 500) {
                return "🦃 " + cal + " kcal: 180g chicken, quinoa, steamed broccoli";
            } else {
                return "🦃 " + cal + " kcal: 150g fish, vegetables, brown rice";
            }
        }
    }

    private String generateDynamicSnacks(User user, double calories) {
        int cal = (int) Math.round(calories);
        String goal = user.getWeightGoal();

        if (goal.equalsIgnoreCase("GAIN")) {
            if (cal > 300) {
                return "🥜 " + cal + " kcal: Trail mix, protein shake, peanut butter sandwich";
            } else if (cal > 200) {
                return "🥜 " + cal + " kcal: Greek yogurt with berries, handful of almonds";
            } else {
                return "🥜 " + cal + " kcal: Banana with peanut butter, protein bar";
            }
        } else if (goal.equalsIgnoreCase("LOSS")) {
            if (cal > 200) {
                return "🥛 " + cal + " kcal: Greek yogurt, apple, handful of almonds";
            } else if (cal > 100) {
                return "🥛 " + cal + " kcal: Apple with peanut butter or Greek yogurt";
            } else {
                return "🥛 " + cal + " kcal: Small apple or 1 boiled egg";
            }
        } else {
            if (cal > 250) {
                return "🍎 " + cal + " kcal: Greek yogurt, nuts, fruit";
            } else if (cal > 150) {
                return "🍎 " + cal + " kcal: Apple with peanut butter";
            } else {
                return "🍎 " + cal + " kcal: Handful of berries or small yogurt";
            }
        }
    }

    private String generateDynamicDescription(User user, String bmiCategory, double calories) {
        String goal = user.getWeightGoal();
        int cal = (int) Math.round(calories);
        double weight = user.getWeight();

        if (goal.equalsIgnoreCase("GAIN")) {
            if (weight < 60) {
                return "📈 Calorie surplus needed. Focus on calorie-dense foods. Daily intake: " + cal + " kcal.";
            } else {
                return "💪 " + cal + " kcal daily for lean muscle gain. Eat every 3-4 hours.";
            }
        } else if (goal.equalsIgnoreCase("LOSS")) {
            if (bmiCategory.equals("Obese")) {
                return "🏃‍♂️ " + cal + " kcal daily for sustainable weight loss. Focus on portion control.";
            } else if (bmiCategory.equals("Overweight")) {
                return "⚖️ " + cal + " kcal daily for healthy weight loss. Balance your plate.";
            } else {
                return "🎯 " + cal + " kcal daily for gradual weight loss.";
            }
        } else {
            return "⚖️ " + cal + " kcal daily for weight maintenance.";
        }
    }

    private List<String> generateDynamicTips(User user, String bmiCategory) {
        List<String> tips = new ArrayList<>();
        String goal = user.getWeightGoal();

        if (goal.equalsIgnoreCase("GAIN")) {
            tips.add("Add healthy fats: olive oil, nuts, avocado to increase calories");
            tips.add("Eat protein with every meal (30-40g per meal)");
            tips.add("Focus on progressive overload in your workouts");
        } else if (goal.equalsIgnoreCase("LOSS")) {
            tips.add("Drink water before meals to feel fuller");
            tips.add("Prioritize protein to preserve muscle");
            tips.add("Increase daily steps (aim for 8,000-10,000)");
            if (bmiCategory.equals("Obese")) {
                tips.add("Start with walking 30 minutes daily");
            }
        } else {
            tips.add("Balance your plate: 1/4 protein, 1/4 carbs, 1/2 vegetables");
            tips.add("Stay consistent with your meal timing");
        }
        return tips;
    }

    private FitnessPlanResponse.ExercisePlan generateDynamicExercisePlan(String workoutFrequency, String goal, double weight) {
        FitnessPlanResponse.ExercisePlan exercisePlan = new FitnessPlanResponse.ExercisePlan();

        if (goal.equalsIgnoreCase("GAIN")) {
            exercisePlan.setCardio("Light cardio warm-up only (10 mins)");
            exercisePlan.setStrength("Heavy compound lifts: Squats, Deadlifts, Bench Press");
            exercisePlan.setFrequency("4-5 times per week");
            exercisePlan.setDuration("60-75 minutes per session");
            exercisePlan.setIntensity("High intensity, 8-12 reps");
        } else if (goal.equalsIgnoreCase("MAINTAIN")) {
            exercisePlan.setCardio("Moderate cardio - 20-30 minutes, 3 times per week");
            exercisePlan.setStrength("Full body strength training, 3-4 times per week");
            exercisePlan.setFrequency("4-5 times per week total");
            exercisePlan.setDuration("45-60 minutes per session");
            exercisePlan.setIntensity("Moderate to high intensity");
        } else {
            if (weight > 90) {
                exercisePlan.setCardio("Walking - 40-50 minutes daily");
                exercisePlan.setStrength("Bodyweight exercises, swimming");
                exercisePlan.setFrequency("Daily walking + 3 strength sessions");
                exercisePlan.setDuration("45-60 minutes total per day");
                exercisePlan.setIntensity("Low to moderate");
            } else {
                switch (workoutFrequency.toLowerCase()) {
                    case "sedentary":
                        exercisePlan.setCardio("Walking - 30-40 minutes daily");
                        exercisePlan.setStrength("Bodyweight exercises - 15-20 minutes, 3 times/week");
                        exercisePlan.setFrequency("Daily walking + 3 strength sessions");
                        exercisePlan.setDuration("45-60 minutes total per day");
                        exercisePlan.setIntensity("Low to moderate");
                        break;
                    case "moderate":
                        exercisePlan.setCardio("Jogging or Cycling - 30-40 minutes");
                        exercisePlan.setStrength("Dumbbell workouts, Push-ups, Squats");
                        exercisePlan.setFrequency("4-5 times per week");
                        exercisePlan.setDuration("45-60 minutes per session");
                        exercisePlan.setIntensity("Moderate to high");
                        break;
                    default:
                        exercisePlan.setCardio("HIIT or Running - 20-30 minutes, 3 times/week");
                        exercisePlan.setStrength("Full body strength training, 3-4 times/week");
                        exercisePlan.setFrequency("5-6 times per week");
                        exercisePlan.setDuration("45-60 minutes per session");
                        exercisePlan.setIntensity("High intensity");
                }
            }
        }
        return exercisePlan;
    }

    private FitnessPlanResponse.NutritionInfo generateNutritionInfo(double calories, String goal, double weight,
                                                                    double protein, double carbs, double fats) {
        FitnessPlanResponse.NutritionInfo info = new FitnessPlanResponse.NutritionInfo();

        info.setProtein(Math.round(protein));
        info.setCarbs(Math.round(carbs));
        info.setFats(Math.round(fats));
        info.setFiber(goal.equalsIgnoreCase("GAIN") ? 35 : (goal.equalsIgnoreCase("LOSS") ? 25 : 30));

        if (goal.equalsIgnoreCase("GAIN")) {
            info.setProteinSources("Chicken, eggs, beef, fish, whey protein, Greek yogurt");
            info.setCarbSources("Brown rice, oats, sweet potatoes, quinoa, whole grain bread");
            info.setFatSources("Avocado, nuts, olive oil, peanut butter");
        } else if (goal.equalsIgnoreCase("MAINTAIN")) {
            info.setProteinSources("Chicken, fish, eggs, legumes, Greek yogurt");
            info.setCarbSources("Whole grains, vegetables, fruits, oats");
            info.setFatSources("Olive oil, nuts, seeds, avocado");
        } else {
            info.setProteinSources("Lean chicken, fish, egg whites, tofu, lentils");
            info.setCarbSources("Vegetables, berries, oats, quinoa, sweet potatoes");
            info.setFatSources("Avocado, olive oil, nuts in moderation");
        }
        return info;
    }

    private List<String> generateDynamicRecommendations(User user, double targetCalories, String bmiCategory) {
        List<String> recommendations = new ArrayList<>();
        double waterLiters = user.getWeight() * 0.033;
        recommendations.add(String.format("💧 Drink %.1f liters of water daily", waterLiters));
        recommendations.add("😴 Aim for 7-9 hours of quality sleep");

        double proteinGrams = user.getWeight() * (user.getWeightGoal().equalsIgnoreCase("GAIN") ? 2.0 : 1.8);
        recommendations.add(String.format("🍗 Consume %.0f grams of protein daily", proteinGrams));

        if (user.getWeightGoal().equalsIgnoreCase("GAIN")) {
            recommendations.add("📈 Eat in a calorie surplus of 300-500 calories daily");
            recommendations.add("🏋️ Focus on progressive overload");
            recommendations.add("🥄 Eat every 3-4 hours");
        } else if (user.getWeightGoal().equalsIgnoreCase("MAINTAIN")) {
            recommendations.add("⚖️ Monitor weight weekly");
            recommendations.add("🍽️ Practice mindful eating");
        } else {
            recommendations.add("📉 Target weight loss: 0.5-1 kg per week");
            recommendations.add("🍽️ Practice portion control");
            recommendations.add("🚶 Increase daily steps (aim for 8,000-10,000)");
            if (bmiCategory.equals("Obese")) {
                recommendations.add("🏊 Start with low-impact exercises");
            }
        }
        recommendations.add("📊 Take progress photos monthly");
        return recommendations;
    }

    public double calculateBMI(double weight, double height) {
        double heightInMeters = height / 100;
        return weight / (heightInMeters * heightInMeters);
    }

    public String getBMICategory(double bmi) {
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25) return "Normal weight";
        if (bmi < 30) return "Overweight";
        return "Obese";
    }

    // ========== DELETE USER ==========
    @Transactional
    public boolean deleteUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Progress> userProgress = progressRepository.findByUserEmailOrderByDateDesc(email);
        if (!userProgress.isEmpty()) progressRepository.deleteAll(userProgress);

        List<Achievement> userAchievements = achievementRepository.findByUserEmailOrderByEarnedDateDesc(email);
        if (!userAchievements.isEmpty()) achievementRepository.deleteAll(userAchievements);

        List<WorkoutLog> userWorkouts = workoutRepository.findByUserEmailOrderByDateDesc(email);
        if (!userWorkouts.isEmpty()) workoutRepository.deleteAll(userWorkouts);

        streakRepository.findByUserEmail(email).ifPresent(streak -> streakRepository.delete(streak));

        List<MealLog> userMeals = mealLogRepository.findByUserEmailOrderByDateDesc(email);
        if (!userMeals.isEmpty()) mealLogRepository.deleteAll(userMeals);

        List<SleepLog> userSleep = sleepLogRepository.findByUserEmailOrderByDateDesc(email);
        if (!userSleep.isEmpty()) sleepLogRepository.deleteAll(userSleep);

        List<StepLog> userSteps = stepLogRepository.findByUserEmailOrderByDateDesc(email);
        if (!userSteps.isEmpty()) stepLogRepository.deleteAll(userSteps);

        List<ProgressPhoto> userPhotos = photoRepository.findByUserEmailOrderByDateDesc(email);
        if (!userPhotos.isEmpty()) photoRepository.deleteAll(userPhotos);

        List<BodyMeasurement> userMeasurements = measurementRepository.findByUserEmailOrderByDateDesc(email);
        if (!userMeasurements.isEmpty()) measurementRepository.deleteAll(userMeasurements);

        userRepository.delete(user);
        return true;
    }

    // ========== PROGRESS METHODS ==========
    public Progress addProgress(String email, Double weight, Double bodyFat, Double muscleMass, String notes) {
        Progress progress = new Progress();
        progress.setUserEmail(email);
        progress.setWeight(weight);
        progress.setBodyFat(bodyFat);
        progress.setMuscleMass(muscleMass);
        progress.setNotes(notes);
        progress.setDate(LocalDate.now());
        return progressRepository.save(progress);
    }

    public Map<String, Object> getProgressStats(String email) {
        List<Progress> progressList = progressRepository.findByUserEmailOrderByDateAsc(email);
        Map<String, Object> stats = new HashMap<>();
        if (progressList.isEmpty()) {
            stats.put("totalEntries", 0);
            stats.put("weightChange", 0);
            stats.put("startWeight", 0);
            stats.put("currentWeight", 0);
            return stats;
        }
        Progress first = progressList.get(0);
        Progress last = progressList.get(progressList.size() - 1);
        stats.put("totalEntries", progressList.size());
        stats.put("weightChange", last.getWeight() - first.getWeight());
        stats.put("startWeight", first.getWeight());
        stats.put("currentWeight", last.getWeight());
        return stats;
    }

    // ========== ACHIEVEMENT METHODS ==========
    public List<Achievement> checkAndAwardAchievements(String email) {
        List<Achievement> newAchievements = new ArrayList<>();
        long workoutCount = workoutRepository.countByUserEmail(email);

        if (workoutCount >= 1 && !achievementRepository.existsByUserEmailAndBadgeName(email, "First Workout")) {
            Achievement a = new Achievement();
            a.setUserEmail(email);
            a.setBadgeName("First Workout");
            a.setDescription("Completed your first workout!");
            a.setIcon("🏃‍♂️");
            achievementRepository.save(a);
            newAchievements.add(a);
        }
        if (workoutCount >= 5 && !achievementRepository.existsByUserEmailAndBadgeName(email, "5 Workouts")) {
            Achievement a = new Achievement();
            a.setUserEmail(email);
            a.setBadgeName("5 Workouts");
            a.setDescription("Completed 5 workouts!");
            a.setIcon("🔥");
            achievementRepository.save(a);
            newAchievements.add(a);
        }
        if (workoutCount >= 10 && !achievementRepository.existsByUserEmailAndBadgeName(email, "10 Workouts")) {
            Achievement a = new Achievement();
            a.setUserEmail(email);
            a.setBadgeName("10 Workouts");
            a.setDescription("Completed 10 workouts!");
            a.setIcon("💪");
            achievementRepository.save(a);
            newAchievements.add(a);
        }

        LocalDateTime weekAgo = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
        long workoutsThisWeek = workoutRepository.countByUserEmailAndDateAfter(email, weekAgo);
        if (workoutsThisWeek >= 7 && !achievementRepository.existsByUserEmailAndBadgeName(email, "7-Day Warrior")) {
            Achievement a = new Achievement();
            a.setUserEmail(email);
            a.setBadgeName("7-Day Warrior");
            a.setDescription("Worked out 7 days in a row!");
            a.setIcon("⚡");
            achievementRepository.save(a);
            newAchievements.add(a);
        }

        List<Progress> progressList = progressRepository.findByUserEmailOrderByDateAsc(email);
        if (progressList.size() >= 2) {
            Progress first = progressList.get(0);
            Progress last = progressList.get(progressList.size() - 1);
            double weightLost = first.getWeight() - last.getWeight();
            if (weightLost >= 5 && !achievementRepository.existsByUserEmailAndBadgeName(email, "5kg Lost")) {
                Achievement a = new Achievement();
                a.setUserEmail(email);
                a.setBadgeName("5kg Lost");
                a.setDescription("Lost 5kg!");
                a.setIcon("🎉");
                achievementRepository.save(a);
                newAchievements.add(a);
            }
            if (weightLost >= 10 && !achievementRepository.existsByUserEmailAndBadgeName(email, "10kg Lost")) {
                Achievement a = new Achievement();
                a.setUserEmail(email);
                a.setBadgeName("10kg Lost");
                a.setDescription("Lost 10kg!");
                a.setIcon("🏆");
                achievementRepository.save(a);
                newAchievements.add(a);
            }
        }
        return newAchievements;
    }

    public List<Achievement> getUserAchievements(String email) {
        return achievementRepository.findByUserEmailOrderByEarnedDateDesc(email);
    }

    // ========== STREAK METHODS ==========
    public void updateStreak(String email, boolean hasActivity) {
        UserStreak streak = streakRepository.findByUserEmail(email).orElse(new UserStreak());
        streak.setUserEmail(email);
        LocalDate today = LocalDate.now();
        if (hasActivity) {
            if (streak.getLastActivityDate() != null) {
                LocalDate yesterday = today.minusDays(1);
                if (streak.getLastActivityDate().equals(yesterday)) {
                    streak.setCurrentStreak(streak.getCurrentStreak() + 1);
                } else if (!streak.getLastActivityDate().equals(today)) {
                    streak.setCurrentStreak(1);
                }
            } else {
                streak.setCurrentStreak(1);
            }
            if (streak.getCurrentStreak() > streak.getLongestStreak()) {
                streak.setLongestStreak(streak.getCurrentStreak());
            }
            streak.setLastActivityDate(today);
            streakRepository.save(streak);
        }
    }

    public Map<String, Object> getUserStreak(String email) {
        UserStreak streak = streakRepository.findByUserEmail(email).orElse(new UserStreak());
        Map<String, Object> result = new HashMap<>();
        result.put("currentStreak", streak.getCurrentStreak() != null ? streak.getCurrentStreak() : 0);
        result.put("longestStreak", streak.getLongestStreak() != null ? streak.getLongestStreak() : 0);
        result.put("activeToday", streak.getLastActivityDate() != null && streak.getLastActivityDate().equals(LocalDate.now()));
        return result;
    }

    // ========== BMI METHODS ==========
    public Map<String, Object> getUserBMI(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        double bmi = calculateBMI(user.getWeight(), user.getHeight());
        Map<String, Object> result = new HashMap<>();
        result.put("bmi", Math.round(bmi * 10) / 10.0);
        result.put("category", getBMICategory(bmi));
        result.put("weight", user.getWeight());
        result.put("height", user.getHeight());
        return result;
    }
}