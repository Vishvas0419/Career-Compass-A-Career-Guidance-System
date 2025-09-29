package com.example.cgs.controller;

import com.example.cgs.entities.Courses;
import com.example.cgs.repositories.CoursesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CoursesRepository coursesRepository;

    /**
     * Fetch all courses.
     *
     * @return List of all courses.
     */
    @GetMapping
    public ResponseEntity<List<Courses>> getAllCourses() {
        List<Courses> courses = coursesRepository.findAll();
        return ResponseEntity.ok(courses);
    }

    /**
     * Fetch a specific course by ID.
     *
     * @param id The ID of the course.
     * @return The course details if found, or a 404 response.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Courses> getCourseById(@PathVariable Long id) {
        return coursesRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Add a new course with its associated skills.
     *
     * @param course The course details with its skills.
     * @return The saved course.
     */
    @PostMapping
    public ResponseEntity<Courses> addCourse(@RequestBody Courses course) {
        // Debug print
        System.out.println("Adding new course: " + course.getCourseTitle());
        System.out.println("Course skills: " + course.getSkills().stream()
            .map(skill -> skill.getSkill())
            .collect(Collectors.joining(", ")));
        
        return ResponseEntity.ok(coursesRepository.save(course));
    }

    /**
     * Update an existing course by ID.
     *
     * @param id     The ID of the course to update.
     * @param course The updated course details.
     * @return The updated course or a 404 response if not found.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Courses> updateCourse(@PathVariable Long id, @RequestBody Courses course) {
        System.out.println("Updating course with ID: " + id);
        System.out.println("Course data received: " + course.getCourseTitle());
        System.out.println("Trainer name: " + course.getName());
        System.out.println("Skills count: " + (course.getSkills() != null ? course.getSkills().size() : "null"));
        
        var existingCourseOpt = coursesRepository.findById(id);
        if (existingCourseOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Courses existingCourse = existingCourseOpt.get();
        System.out.println("Found existing course: " + existingCourse.getCourseTitle());
        
        try {
            // Update basic course information
            existingCourse.setName(course.getName());
            existingCourse.setImageUrl(course.getImageUrl());
            existingCourse.setCoverImage(course.getCoverImage());
            existingCourse.setTrainerDesignation(course.getTrainerDesignation());
            existingCourse.setCourseTitle(course.getCourseTitle());
            existingCourse.setDescription(course.getDescription());
            
            // Handle skills update properly
            if (course.getSkills() != null) {
                // Clear existing skills (orphanRemoval will handle deletion)
                existingCourse.getSkills().clear();
                // Add new skills
                existingCourse.getSkills().addAll(course.getSkills());
            }
            
            Courses updatedCourse = coursesRepository.save(existingCourse);
            System.out.println("Course updated successfully: " + updatedCourse.getCourseTitle());
            return ResponseEntity.ok(updatedCourse);
        } catch (Exception e) {
            System.err.println("Error saving course: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Delete a course by ID.
     *
     * @param id The ID of the course to delete.
     * @return A 204 response if deleted successfully, or 404 if not found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCourse(@PathVariable Long id) {
        return coursesRepository.findById(id)
                .map(course -> {
                    coursesRepository.delete(course);
                    return ResponseEntity.ok("deleted the course"); // Explicitly set type to Void
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Fetch courses that teach a specific skill.
     *
     * @param skill The skill name to search for.
     * @return List of courses that teach the specified skill.
     */
    @GetMapping("/by-skill")
    public ResponseEntity<List<Courses>> getCoursesBySkill(@RequestParam String skill) {
        System.out.println("Searching for courses with skill: " + skill);
        
        List<Courses> allCourses = coursesRepository.findAll();
        List<Courses> matchingCourses = allCourses.stream()
                .filter(course -> course.getSkills() != null && 
                        course.getSkills().stream()
                                .anyMatch(courseSkill -> 
                                    courseSkill.getSkill().toLowerCase().contains(skill.toLowerCase()) ||
                                    skill.toLowerCase().contains(courseSkill.getSkill().toLowerCase())
                                ))
                .collect(Collectors.toList());
        
        System.out.println("Found " + matchingCourses.size() + " courses for skill: " + skill);
        return ResponseEntity.ok(matchingCourses);
    }
}

