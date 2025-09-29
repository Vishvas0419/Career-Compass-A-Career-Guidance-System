package com.example.cgs.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cgs.entities.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    UserProfile findByEmail(String email);
}
