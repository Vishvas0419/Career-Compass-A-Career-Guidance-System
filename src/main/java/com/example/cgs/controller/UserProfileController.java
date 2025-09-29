package com.example.cgs.controller;


import com.example.cgs.entities.UserProfile;
import com.example.cgs.repositories.UserProfileRepository;
import com.example.cgs.service.UserProfileService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/profiles")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserProfileRepository userProfileRepository;

    // Save User Profile
    @PostMapping
    public ResponseEntity<UserProfile> saveOrUpdateUserProfile(@RequestBody UserProfile userProfile, HttpSession session) {

        String email = (String) session.getAttribute("userEmail");
        userProfile.setEmail(email);

        // Check if the user profile exists based on email
        UserProfile existingProfile = userProfileRepository.findByEmail(email);

        if (existingProfile != null) {
            // Update existing profile
            existingProfile.setName(userProfile.getName());
            existingProfile.setCareerGoal(userProfile.getCareerGoal());

            // Update skills list
            if (userProfile.getSkills() != null) {
                existingProfile.getSkills().clear(); // Clear existing skills
                existingProfile.getSkills().addAll(userProfile.getSkills()); // Add new skills
            }

            UserProfile updatedProfile = userProfileRepository.save(existingProfile);
            return ResponseEntity.ok(updatedProfile);
        } else {
            // Create new profile
            UserProfile newProfile = userProfileRepository.save(userProfile);
            return ResponseEntity.ok(newProfile);
        }
    }

    // Get All User Profiles
    @GetMapping
    public ResponseEntity<List<UserProfile>> getAllUserProfiles() {
        List<UserProfile> profiles = userProfileService.getAllUserProfiles();
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/skills")
    public ResponseEntity<List<String>> getUserSkills(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.badRequest().body(null); // Email not in session
        }

        // Retrieve user profile by email
        UserProfile userProfile = userProfileService.getUserProfileByEmail(email);
        if (userProfile == null) {
            return ResponseEntity.notFound().build(); // No user found for the email
        }

        // Extract and return skills
        List<String> skills = userProfile.getSkills().stream()
                .map(skill -> skill.getSkill() + " (" + skill.getLevel() + ")")
                .toList();

        return ResponseEntity.ok(skills);
    }

    @GetMapping("/user-profile")
    public ResponseEntity<UserProfile> getUserProfile(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.badRequest().body(null); // Email not in session
        }

        // Retrieve user profile by email
        UserProfile userProfile = userProfileService.getUserProfileByEmail(email);
        if (userProfile == null) {
            return ResponseEntity.notFound().build(); // No user found for the email
        }

        return ResponseEntity.ok(userProfile);
    }

    @PutMapping("/update-career-goal")
    public ResponseEntity<String> updateCareerGoal(@RequestBody UserProfile careerGoalUpdate, HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.badRequest().body("User not logged in");
        }

        // Retrieve user profile by email
        UserProfile userProfile = userProfileService.getUserProfileByEmail(email);
        if (userProfile == null) {
            return ResponseEntity.notFound().build();
        }

        // Update career goal
        userProfile.setCareerGoal(careerGoalUpdate.getCareerGoal());
        userProfileRepository.save(userProfile);

        return ResponseEntity.ok("Career goal updated successfully");
    }

}
