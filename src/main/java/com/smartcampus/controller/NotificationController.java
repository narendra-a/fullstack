package com.smartcampus.controller;

import com.smartcampus.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/student/notifications")
    public String viewNotifications(HttpSession session, Model model) {
        String email = (String) session.getAttribute("studentEmail");
        if (email == null) return "redirect:/student/login";

        model.addAttribute("notifications", notificationService.getNotificationsForStudent(email));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(email));
        model.addAttribute("studentName", session.getAttribute("studentName"));
        notificationService.markAllRead(email);
        return "notifications";
    }

    @PostMapping("/student/notifications/read")
    public String markRead(HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("studentEmail");
        if (email != null) notificationService.markAllRead(email);
        redirectAttributes.addFlashAttribute("successMessage", "All notifications marked as read.");
        return "redirect:/student/notifications";
    }

    /**
     * AJAX endpoint for the notification bell badge count.
     */
    @GetMapping("/api/notifications/count")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> getUnreadCount(HttpSession session) {
        String email = (String) session.getAttribute("studentEmail");
        long count = email != null ? notificationService.getUnreadCount(email) : 0;
        return ResponseEntity.ok(Map.of("count", count));
    }
}
