package com.example.cgs.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cgs.entities.Users;

public interface UsersRepository extends JpaRepository<Users,Long> {

	boolean existsByEmail(String email);

	Users findByEmail(String email);
	
}
