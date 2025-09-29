package com.example.cgs.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cgs.entities.Job;

public interface JobRepository extends JpaRepository<Job,Long> {

}
