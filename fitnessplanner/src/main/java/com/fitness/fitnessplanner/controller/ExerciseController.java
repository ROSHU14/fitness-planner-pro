package com.fitness.fitnessplanner.controller;

import com.fitness.fitnessplanner.model.Exercise;
import com.fitness.fitnessplanner.repository.ExerciseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exercises")
@CrossOrigin(origins = "*")
public class ExerciseController {

    @Autowired
    private ExerciseRepository exerciseRepository;

    @GetMapping
    public ResponseEntity<?> getAllExercises() {
        try {
            List<Exercise> exercises = exerciseRepository.findAll();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", exercises);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/init")
    public ResponseEntity<?> initializeExercises() {
        try {
            exerciseRepository.deleteAll();

            // Add simple exercises with working videos
            addExercise("Push-up", "Chest", "Chest, Shoulders, Triceps", "Bodyweight",
                    "Start in plank position, lower chest to ground, push back up.",
                    "https://www.youtube.com/embed/IODxDxX7oi4", 2);

            addExercise("Squat", "Legs", "Quadriceps, Hamstrings, Glutes", "Bodyweight",
                    "Stand feet shoulder-width, lower hips back and down, return to standing.",
                    "https://www.youtube.com/embed/YaXPRqUwItQ", 2);

            addExercise("Pull-up", "Back", "Lats, Biceps, Rhomboids", "Pull-up Bar",
                    "Hang from bar, pull chest to bar, lower with control.",
                    "https://www.youtube.com/embed/eGo4IYlbE5g", 4);

            addExercise("Bench Press", "Chest", "Pectorals, Triceps, Front Delts", "Barbell",
                    "Lie on bench, lower bar to chest, press up explosively.",
                    "https://www.youtube.com/embed/4Y2ZdHCOXok", 3);

            addExercise("Deadlift", "Back", "Hamstrings, Glutes, Lower Back", "Barbell",
                    "Hinge at hips, grasp bar, drive through heels to stand tall.",
                    "https://www.youtube.com/embed/op9kVnSso6Q", 4);

            addExercise("Shoulder Press", "Shoulders", "Deltoids, Triceps", "Dumbbell",
                    "Press weights overhead from shoulder height, lower with control.",
                    "https://www.youtube.com/embed/qEwKCR5JCog", 3);

            addExercise("Plank", "Core", "Abs, Lower Back, Shoulders", "Bodyweight",
                    "Hold push-up position, engage core, keep body straight.",
                    "https://www.youtube.com/embed/pSHjTRCQxIw", 2);

            addExercise("Lunges", "Legs", "Quadriceps, Glutes, Hamstrings", "Bodyweight",
                    "Step forward, lower hips, return to start, alternate legs.",
                    "https://www.youtube.com/embed/QOVaHwm-Q6U", 2);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Exercises initialized!");
            response.put("count", 8);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    private void addExercise(String name, String category, String muscleGroup, String equipment,
                             String instructions, String videoUrl, int difficulty) {
        Exercise exercise = new Exercise();
        exercise.setName(name);
        exercise.setCategory(category);
        exercise.setMuscleGroup(muscleGroup);
        exercise.setEquipment(equipment);
        exercise.setInstructions(instructions);
        exercise.setVideoUrl(videoUrl);
        exercise.setDifficulty(difficulty);
        exerciseRepository.save(exercise);
    }
}