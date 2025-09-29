package com.example.cgs.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cgs.entities.Profile;

public interface ProfilesRepository extends JpaRepository<Profile,Long> {

	Profile findByEmail(String attribute);

}
