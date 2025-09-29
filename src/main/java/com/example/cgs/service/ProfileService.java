package com.example.cgs.service;

import com.example.cgs.entities.Profile;
import com.example.cgs.repositories.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    public Profile getProfileByEmail(String email) {
        return profileRepository.findByEmail(email).orElse(null);
    }

    public Profile saveProfile(Profile profile) {
        return profileRepository.save(profile);
    }
}
