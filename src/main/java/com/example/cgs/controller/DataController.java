package com.example.cgs.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/data")
public class DataController {

    @Autowired
    private ResourceLoader resourceLoader;

    @GetMapping("/job-skills-mapping")
    public ResponseEntity<String> getJobSkillsMapping() {
        try {
            Resource resource = resourceLoader.getResource("classpath:static/data/job-skills-mapping.json");
            byte[] bytes = resource.getInputStream().readAllBytes();
            String content = new String(bytes, StandardCharsets.UTF_8);
            return ResponseEntity.ok(content);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error loading job skills mapping: " + e.getMessage());
        }
    }
}
