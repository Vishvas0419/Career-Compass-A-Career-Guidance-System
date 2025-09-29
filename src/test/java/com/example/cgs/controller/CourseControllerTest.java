package com.example.cgs.controller;

import com.example.cgs.entities.Courses;
import com.example.cgs.repositories.CoursesRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
public class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CoursesRepository coursesRepository;

    @Test
    public void testGetAllCourses() throws Exception {
        Courses course1 = new Courses();
        course1.setId(1L);
        course1.setCourseTitle("Java Basics");

        Courses course2 = new Courses();
        course2.setId(2L);
        course2.setCourseTitle("Spring Boot");

        when(coursesRepository.findAll()).thenReturn(Arrays.asList(course1, course2));

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].courseTitle").value("Java Basics"));
    }

    @Test
    public void testGetCourseById_Found() throws Exception {
        Courses course = new Courses();
        course.setId(1L);
        course.setCourseTitle("Java Basics");

        when(coursesRepository.findById(1L)).thenReturn(Optional.of(course));

        mockMvc.perform(get("/api/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseTitle").value("Java Basics"));
    }

    @Test
    public void testGetCourseById_NotFound() throws Exception {
        when(coursesRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/courses/1"))
                .andExpect(status().isNotFound());
    }
}