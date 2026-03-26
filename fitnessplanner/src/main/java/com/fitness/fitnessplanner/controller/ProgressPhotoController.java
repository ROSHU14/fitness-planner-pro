package com.fitness.fitnessplanner.controller;

import com.fitness.fitnessplanner.model.ProgressPhoto;
import com.fitness.fitnessplanner.repository.ProgressPhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/photos")
@CrossOrigin(origins = "*")
public class ProgressPhotoController {

    @Autowired
    private ProgressPhotoRepository photoRepository;

    @PostMapping("/add")
    public ResponseEntity<?> addPhoto(@RequestBody Map<String, Object> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            ProgressPhoto photo = new ProgressPhoto();
            photo.setUserEmail(email);
            photo.setPhotoUrl((String) request.get("photoUrl"));
            photo.setPhotoType((String) request.get("photoType"));
            photo.setNotes(request.get("notes") != null ? request.get("notes").toString() : "");

            ProgressPhoto saved = photoRepository.save(photo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Photo saved successfully");
            response.put("data", saved);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getPhotoHistory() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            List<ProgressPhoto> photos = photoRepository.findByUserEmailOrderByDateDesc(email);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", photos);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}