package com.example.cgs.entities;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Courses {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Trainer Name
    private String imageUrl; // Trainer Image URL
    private String coverImage; // Course Cover Image
    private String trainerDesignation; // Trainer Designation
    private String courseTitle; // Course Title

    @Column(length = 5000)
    private String description; // Course Description

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "course_id") // Foreign key in Skill table
    private List<Skill> skills; // List of skills required for the course

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getTrainerDesignation() {
        return trainerDesignation;
    }

    public void setTrainerDesignation(String trainerDesignation) {
        this.trainerDesignation = trainerDesignation;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }
}