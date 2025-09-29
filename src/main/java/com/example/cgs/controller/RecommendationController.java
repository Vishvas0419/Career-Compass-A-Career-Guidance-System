package com.example.cgs.controller;

import com.example.cgs.entities.Courses;
import com.example.cgs.entities.Skill;
import com.example.cgs.entities.UserProfile;
import com.example.cgs.repositories.CoursesRepository;
import com.example.cgs.repositories.UserProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class RecommendationController {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private CoursesRepository coursesRepository;

    @Autowired
    private ResourceLoader resourceLoader;

    private static class JobSkillsMapping {
        public List<JobMapping> jobSkillsMapping;
    }

    private static class JobMapping {
        public String jobTitle;
        public List<String> requiredSkills;
    }

    /**
     * Recommends courses based on the user's skills.
     *
     * @param session The HTTP session to retrieve the user's email.
     * @return A list of recommended courses sorted by match score.
     */
    @GetMapping("/api/recommend-courses")
    public ResponseEntity<List<Courses>> recommendCourses(HttpSession session) {
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail == null) {
            System.out.println("User email not found in session");
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        // Fetch user profile
        UserProfile user = userProfileRepository.findByEmail(userEmail);
        if (user == null) {
            System.out.println("User profile not found");
            return ResponseEntity.ok(Collections.emptyList());
        }

        try {
            // Get all available courses regardless of user profile
            List<Courses> allCourses = coursesRepository.findAll();
            System.out.println("Total courses found: " + allCourses.size());

            // If no courses are available, return empty list
            if (allCourses.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            // If user has no career goal, recommend courses based on skills the user doesn't have
            if (user.getCareerGoal() == null || user.getCareerGoal().trim().isEmpty()) {
                System.out.println("No career goal specified, recommending courses for missing skills");

                Set<String> missingSkills = findMissingSkillsFromAllCourses(user, allCourses);
                System.out.println("Skills user doesn't have: " + String.join(", ", missingSkills));

                // If user has all possible skills, return empty list
                if (missingSkills.isEmpty()) {
                    System.out.println("User already has all available skills");
                    return ResponseEntity.ok(Collections.emptyList());
                }

                List<Courses> recommendedCourses = recommendCoursesForMissingSkills(allCourses, missingSkills);
                System.out.println("Recommended courses count: " + recommendedCourses.size());
                return ResponseEntity.ok(recommendedCourses);
            }

            // Debug print user's current skills
            System.out.println("User Skills: " + (user.getSkills() != null ? user.getSkills().stream()
                .map(skill -> skill.getSkill())
                .collect(Collectors.joining(", ")) : "None"));
            System.out.println("Career Goal: " + user.getCareerGoal());

            ObjectMapper mapper = new ObjectMapper();
            JobSkillsMapping jobSkillsMapping = mapper.readValue(
                resourceLoader.getResource("classpath:static/data/job-skills-mapping.json").getInputStream(),
                JobSkillsMapping.class
            );

            // Normalize the career goal for more flexible matching
            String normalizedCareerGoal = user.getCareerGoal().toLowerCase().replaceAll("\\s+", " ").trim();

            // Try to find a matching job title with more flexible matching
            Optional<JobMapping> targetJob = jobSkillsMapping.jobSkillsMapping.stream()
                .filter(job -> {
                    String normalizedJobTitle = job.jobTitle.toLowerCase().replaceAll("\\s+", " ").trim();
                    return normalizedJobTitle.equals(normalizedCareerGoal) ||
                           normalizedJobTitle.replace(" ", "").equals(normalizedCareerGoal.replace(" ", ""));
                })
                .findFirst();

            // If no exact match, try partial matching
            if (targetJob.isEmpty()) {
                targetJob = jobSkillsMapping.jobSkillsMapping.stream()
                    .filter(job -> {
                        String normalizedJobTitle = job.jobTitle.toLowerCase().replaceAll("\\s+", " ").trim();
                        return normalizedJobTitle.contains(normalizedCareerGoal) ||
                               normalizedCareerGoal.contains(normalizedJobTitle);
                    })
                    .findFirst();
            }

            // If still no match, recommend courses based on skills the user doesn't have
            if (targetJob.isEmpty()) {
                System.out.println("No matching job title found for: " + user.getCareerGoal() + ", recommending courses for missing skills");

                Set<String> missingSkills = findMissingSkillsFromAllCourses(user, allCourses);
                System.out.println("Skills user doesn't have: " + String.join(", ", missingSkills));

                // If user has all possible skills, return empty list
                if (missingSkills.isEmpty()) {
                    System.out.println("User already has all available skills");
                    return ResponseEntity.ok(Collections.emptyList());
                }

                List<Courses> recommendedCourses = recommendCoursesForMissingSkills(allCourses, missingSkills);
                System.out.println("Recommended courses count: " + recommendedCourses.size());
                return ResponseEntity.ok(recommendedCourses);
            }

            // Get user's existing skills
            Set<String> userSkills = user.getSkills() != null ? user.getSkills().stream()
                .map(skill -> skill.getSkill().toLowerCase())
                .collect(Collectors.toSet()) : new HashSet<>();

            // Find skills required for the job that the user doesn't have
            Set<String> missingSkills = targetJob.get().requiredSkills.stream()
                .map(String::toLowerCase)
                .filter(skill -> !userSkills.contains(skill))
                .collect(Collectors.toSet());

            System.out.println("Missing Skills for job " + targetJob.get().jobTitle + ": " + String.join(", ", missingSkills));

            // If user already has all required skills for the job
            if (missingSkills.isEmpty()) {
                System.out.println("User already has all required skills for the job: " + targetJob.get().jobTitle);
                return ResponseEntity.ok(Collections.emptyList());
            }

            // Recommend courses that teach the missing skills
            List<Courses> recommendedCourses = recommendCoursesForMissingSkills(allCourses, missingSkills);
            System.out.println("Recommended courses count: " + recommendedCourses.size());
            return ResponseEntity.ok(recommendedCourses);

        } catch (IOException e) {
            System.out.println("Error reading job-skills-mapping.json: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Collections.emptyList());
        } catch (Exception e) {
            System.out.println("Unexpected error in recommendation service: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Collections.emptyList());
        }
    }

    /**
     * Helper method to find skills that the user doesn't have from all available courses.
     *
     * @param user The user profile
     * @param allCourses List of all available courses
     * @return Set of skills the user doesn't have
     */
    private Set<String> findMissingSkillsFromAllCourses(UserProfile user, List<Courses> allCourses) {
        // Get all skills from all courses
        Set<String> allPossibleSkills = new HashSet<>();
        for (Courses course : allCourses) {
            if (course.getSkills() != null) {
                course.getSkills().forEach(skill -> allPossibleSkills.add(skill.getSkill().toLowerCase()));
            }
        }

        // Get user's existing skills
        Set<String> userSkills = user.getSkills() != null ? user.getSkills().stream()
            .map(skill -> skill.getSkill().toLowerCase())
            .collect(Collectors.toSet()) : new HashSet<>();

        // Find skills the user doesn't have
        return allPossibleSkills.stream()
            .filter(skill -> !userSkills.contains(skill))
            .collect(Collectors.toSet());
    }

    /**
     * Helper method to recommend courses based on missing skills.
     *
     * @param allCourses List of all available courses
     * @param missingSkills Set of skills the user doesn't have
     * @return List of recommended courses sorted by relevance
     */
    private List<Courses> recommendCoursesForMissingSkills(List<Courses> allCourses, Set<String> missingSkills) {
        // Score and filter courses based on missing skills
        Map<Courses, Integer> courseScores = new HashMap<>();
        for (Courses course : allCourses) {
            int score = calculateCourseScore(course, missingSkills);
            System.out.println("Course: " + course.getCourseTitle() + ", Score: " + score);
            if (course.getSkills() != null) {
                System.out.println("Course Skills: " + course.getSkills().stream()
                    .map(skill -> skill.getSkill())
                    .collect(Collectors.joining(", ")));
            }
            // Only include courses that teach at least one missing skill
            if (score > 0) {
                courseScores.put(course, score);
            }
        }

        return courseScores.entrySet().stream()
            .sorted(Map.Entry.<Courses, Integer>comparingByValue().reversed())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    private int calculateCourseScore(Courses course, Set<String> missingSkills) {
        if (course.getSkills() == null || course.getSkills().isEmpty()) {
            System.out.println("Warning: Course " + course.getCourseTitle() + " has no skills defined");
            return 0;
        }

        int score = 0;
        for (Skill courseSkill : course.getSkills()) {
            if (missingSkills.contains(courseSkill.getSkill().toLowerCase())) {
                score++;
                System.out.println("Matched skill: " + courseSkill.getSkill() + " for course: " + course.getCourseTitle());
            }
        }
        return score;
    }
}

