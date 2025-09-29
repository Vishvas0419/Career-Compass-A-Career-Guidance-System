package com.example.cgs.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cgs.entities.Courses;

public interface CoursesRepository extends JpaRepository<Courses,Long> {

}
