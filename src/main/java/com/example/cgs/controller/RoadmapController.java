package com.example.cgs.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/roadmap")
public class RoadmapController {

    @GetMapping
    public String roadmapPage(HttpSession session, Model model) {
        // Get user information from session
        String name = (String) session.getAttribute("name");
        String email = (String) session.getAttribute("userEmail");
        String userType = (String) session.getAttribute("userType");
        
        model.addAttribute("name", name);
        model.addAttribute("email", email);
        model.addAttribute("userType", userType);
        
        return "roadmap";
    }
}
