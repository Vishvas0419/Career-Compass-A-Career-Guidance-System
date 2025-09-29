package com.example.cgs.repositories;

import com.example.cgs.entities.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    // Find a skill by its name (to avoid duplicate entries)
    Optional<Skill> findBySkill(String skillName);
}