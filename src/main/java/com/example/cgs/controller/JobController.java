package com.example.cgs.controller;

import com.example.cgs.entities.JobPosting;
import com.example.cgs.entities.Skill;
import com.example.cgs.entities.UserProfile;
import com.example.cgs.repositories.JobPostingRepository;
import com.example.cgs.repositories.UserProfileRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


import java.util.*;
import java.util.stream.Collectors;

@Controller
public class JobController {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private JobPostingRepository jobPostingRepository;

    /**
     * Get job postings matching the user's skills.
     *
     * @param session The HTTP session to retrieve the user's email.
     * @return A list of job postings that match the user's skills.
     */
    @GetMapping("/api/matching-jobs")
    public ResponseEntity<List<JobPosting>> getMatchingJobs(HttpSession session) {
        // Retrieve userEmail from the session
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail == null) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        // Fetch the user's profile using the email
        UserProfile user = userProfileRepository.findByEmail(userEmail);

        List<Skill> userSkills = user.getSkills();

        // Fetch all job postings
        List<JobPosting> jobPostings = jobPostingRepository.findAll();

        // Filter jobs that match at least one of the user's skills
        List<JobPosting> matchingJobs = jobPostings.stream()
                .filter(job -> matchesUserSkills(userSkills, job.getRequiredSkills()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(matchingJobs);
    }

    /**
     * Check if the user's skills match the required skills for a job.
     *
     * @param userSkills   The list of user's skills.
     * @param requiredSkills The required skills for the job (comma-separated string).
     * @return True if the user has at least one matching skill.
     */
    private boolean matchesUserSkills(List<Skill> userSkills, String requiredSkills) {
        // Convert the required skills to a list
        List<String> requiredSkillsList = Arrays.asList(requiredSkills.split(","));

        // Check if any user skill matches the required skills
        return userSkills.stream()
                .anyMatch(userSkill -> requiredSkillsList.stream()
                        .anyMatch(requiredSkill -> userSkill.getSkill().equalsIgnoreCase(requiredSkill.trim())));
    }

    @GetMapping("/api/jobs")
    public ResponseEntity<List<JobPosting>> getAllMessages() {
        List<JobPosting> jobs = jobPostingRepository.findAll();
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/api/jobs/{id}")
    public ResponseEntity<JobPosting> getJobById(@PathVariable Long id) {
        return jobPostingRepository.findById(id)
                .map(job -> ResponseEntity.ok(job))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update job by ID
     * @param id Job ID
     * @param updatedJob Updated job details
     * @return Updated job
     */
    @PutMapping("/api/jobs/{id}")
    public ResponseEntity<JobPosting> updateJob(@PathVariable Long id, @RequestBody JobPosting updatedJob) {
        return jobPostingRepository.findById(id)
                .map(existingJob -> {
                    existingJob.setJobTitle(updatedJob.getJobTitle());
                    existingJob.setCompanyName(updatedJob.getCompanyName());
                    existingJob.setLocation(updatedJob.getLocation());
                    existingJob.setRequiredSkills(updatedJob.getRequiredSkills());
                    existingJob.setSalaryRange(updatedJob.getSalaryRange());
                    existingJob.setJobDescription(updatedJob.getJobDescription());
                    existingJob.setApplyLink(updatedJob.getApplyLink());
                    JobPosting savedJob = jobPostingRepository.save(existingJob); // Use correct repository and entity
                    return ResponseEntity.ok(savedJob); // Return correct entity type
                })
                .orElse(ResponseEntity.notFound().build()); // Handle case where job is not found
    }

    @GetMapping("/editjob/{id}")
    public String editJobPage(@PathVariable Long id) {
        // Return the name of the HTML file (without the .html extension)
        return "editjob";
    }
    
    @DeleteMapping("api/jobs/{id}")
    public ResponseEntity<String> deleteJob(@PathVariable Long id) {
        try {
            // Check if the job exists before deletion
            if (jobPostingRepository.existsById(id)) {
                jobPostingRepository.deleteById(id);
                return ResponseEntity.ok("Job deleted successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Job not found");
            }
        } catch (Exception e) {
            // Log the exception (use a logging framework in production)
            System.err.println("Error occurred while deleting job: " + e.getMessage());
            e.printStackTrace();

            // Return a generic error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while deleting the job");
        }
    }
}

