package com.smartcampus.controller;

import com.smartcampus.entity.StudentPoints;
import com.smartcampus.service.GamificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class GamificationController {

    @Autowired
    private GamificationService gamificationService;

    @GetMapping("/student/points")
    public String studentPoints(HttpSession session, Model model) {
        String email = (String) session.getAttribute("studentEmail");
        if (email == null) return "redirect:/student/login";

        Optional<StudentPoints> sp = gamificationService.getStudentPoints(email);
        int rank = gamificationService.getRank(email);
        List<StudentPoints> leaderboard = gamificationService.getLeaderboard();

        model.addAttribute("studentPoints", sp.orElse(null));
        model.addAttribute("rank", rank);
        model.addAttribute("leaderboard", leaderboard);
        model.addAttribute("studentName", session.getAttribute("studentName"));
        model.addAttribute("studentEmail", email);
        model.addAttribute("pointsRegister", GamificationService.POINTS_REGISTER);
        model.addAttribute("pointsAttend", GamificationService.POINTS_ATTEND);
        model.addAttribute("pointsFeedback", GamificationService.POINTS_FEEDBACK);
        return "student_points";
    }

    @GetMapping("/leaderboard")
    public String leaderboard(Model model, HttpSession session) {
        List<StudentPoints> leaderboard = gamificationService.getLeaderboard();
        model.addAttribute("leaderboard", leaderboard);
        model.addAttribute("studentEmail", session.getAttribute("studentEmail"));
        return "leaderboard";
    }

    /**
     * AJAX endpoint for the navbar points display.
     */
    @GetMapping("/api/points")
    @ResponseBody
    public ResponseEntity<Map<String, Integer>> getPoints(HttpSession session) {
        String email = (String) session.getAttribute("studentEmail");
        int points = 0;
        if (email != null) {
            Optional<StudentPoints> sp = gamificationService.getStudentPoints(email);
            points = sp.map(StudentPoints::getTotalPoints).orElse(0);
        }
        return ResponseEntity.ok(Map.of("points", points));
    }
}

