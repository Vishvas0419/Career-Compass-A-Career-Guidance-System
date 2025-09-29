package com.example.cgs.controller;

import com.example.cgs.entities.Profile;
import com.example.cgs.service.ProfileService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final String UPLOAD_DIR = Paths.get(System.getProperty("user.dir"), "uploads").toString();


    @PostMapping("/update")
    public ResponseEntity<String> updateProfile(
            @RequestParam("email") String email,
            @RequestParam("name") String name,
            @RequestParam("profilePic") MultipartFile profilePic,
            @RequestParam("interests") String interests,
            @RequestParam("role") String role,
            @RequestParam("certification") String certification,
            @RequestParam("achievements") String achievements,
            @RequestParam(value = "careerGoal", required = false) String careerGoal
    ) {
        // Retrieve the profile by email
        Profile profile = profileService.getProfileByEmail(email);

        if (profile == null) {
            return ResponseEntity.badRequest().body("Profile not found.");
        }

        // Update fields
        profile.setName(name);
        profile.setInterests(interests);
        profile.setRole(role);
        profile.setCertification(certification);
        profile.setAchievements(achievements);
        if (careerGoal != null && !careerGoal.trim().isEmpty()) {
            profile.setCareerGoal(careerGoal);
        }

        // Handle profile picture upload
        if (!profilePic.isEmpty()) {
            try {
                Path uploadPath = Paths.get(UPLOAD_DIR);

                // Create the directory if it does not exist
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Define the file path for saving the profile picture
                String profilePicPath = uploadPath.resolve(profilePic.getOriginalFilename()).toString();

                // Save the profile picture to the file system
                profilePic.transferTo(new java.io.File(profilePicPath));

                // Set the file path in the profile
                profile.setProfilePic(profilePicPath);

            } catch (IOException e) {
                return ResponseEntity.internalServerError().body("Error saving profile picture: " + e.getMessage());
            }
        }

        // Save updated profile
        profileService.saveProfile(profile);

        return ResponseEntity.ok("Profile updated successfully.");
    }

    @GetMapping("/profile-pic")
    public ResponseEntity<byte[]> getProfilePicture(HttpSession session) {
        // Fetch email from the session
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.badRequest().build(); // Handle missing email
        }

        // Retrieve the profile by email
        Profile profile = profileService.getProfileByEmail(email);
        if (profile == null || profile.getProfilePic() == null) {
            return ResponseEntity.notFound().build(); // No profile or picture found
        }

        try {
            // Load the image as bytes
            byte[] imageBytes = Files.readAllBytes(Paths.get(profile.getProfilePic()));

            // Return the image as a response
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG) // Or MediaType.IMAGE_PNG based on your format
                    .body(imageBytes);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/interests")
    public ResponseEntity<List<String>> getInterests(HttpSession session) {
        // Fetch email from session
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.badRequest().body(null); // Handle missing email
        }

        // Retrieve the profile by email
        Profile profile = profileService.getProfileByEmail(email);
        if (profile == null || profile.getInterests() == null) {
            return ResponseEntity.ok(List.of()); // Return an empty list if no interests found
        }

        // Split interests string into a list
        List<String> interests = List.of(profile.getInterests().split(","));

        return ResponseEntity.ok(interests);
    }

    @GetMapping("/certifications")
    public ResponseEntity<List<String>> getCertifications(HttpSession session) {
        // Fetch email from session
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.badRequest().body(null); // Handle missing email
        }

        // Retrieve the profile by email
        Profile profile = profileService.getProfileByEmail(email);
        if (profile == null || profile.getCertification() == null) {
            return ResponseEntity.ok(List.of()); // Return an empty list if no certifications found
        }

        // Split certifications string into a list
        List<String> certifications = List.of(profile.getCertification().split(","));

        return ResponseEntity.ok(certifications);
    }

    @GetMapping("/achievements")
    public ResponseEntity<List<String>> getAchievements(HttpSession session) {
        // Fetch email from session
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.badRequest().body(null); // Handle missing email
        }

        // Retrieve the profile by email
        Profile profile = profileService.getProfileByEmail(email);
        if (profile == null || profile.getAchievements() == null) {
            return ResponseEntity.ok(List.of()); // Return an empty list if no achievements found
        }

        // Split achievements string into a list
        List<String> achievements = List.of(profile.getAchievements().split(","));

        return ResponseEntity.ok(achievements);
    }

}