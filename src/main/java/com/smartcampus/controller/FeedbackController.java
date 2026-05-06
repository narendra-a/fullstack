package com.smartcampus.controller;

import com.smartcampus.entity.Feedback;
import com.smartcampus.entity.Event;
import com.smartcampus.service.EventService;
import com.smartcampus.service.FeedbackService;
import com.smartcampus.service.GamificationService;
import com.smartcampus.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private EventService eventService;

    @Autowired
    private GamificationService gamificationService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Student: show feedback form for a past event.
     */
    @GetMapping("/feedback/{eventId}")
    public String showFeedbackForm(@PathVariable Long eventId,
                                   HttpSession session,
                                   Model model) {
        String email = (String) session.getAttribute("studentEmail");
        String name  = (String) session.getAttribute("studentName");
        if (email == null) return "redirect:/student/login";

        Event event = eventService.getEventById(eventId);
        model.addAttribute("event", event);

        boolean alreadySubmitted = feedbackService.hasUserSubmittedFeedback(email, eventId);
        model.addAttribute("alreadySubmitted", alreadySubmitted);

        if (alreadySubmitted) {
            // Show existing feedback
            feedbackService.getFeedbackForEvent(eventId).stream()
                    .filter(f -> f.getStudent().getEmail().equalsIgnoreCase(email))
                    .findFirst()
                    .ifPresent(f -> model.addAttribute("existingFeedback", f));
        }

        Double avg = feedbackService.getAverageRating(eventId);
        model.addAttribute("averageRating", avg);
        model.addAttribute("feedbackList", feedbackService.getFeedbackForEvent(eventId));
        model.addAttribute("studentName", name);
        return "feedback_form";
    }

    /**
     * Student: submit feedback.
     */
    @PostMapping("/feedback/{eventId}")
    public String submitFeedback(@PathVariable Long eventId,
                                 @RequestParam int rating,
                                 @RequestParam(required = false) String comment,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("studentEmail");
        if (email == null) return "redirect:/student/login";

        try {
            feedbackService.submitFeedback(email, eventId, rating, comment);
            gamificationService.awardPoints(email, "FEEDBACK");
            String name = (String) session.getAttribute("studentName");
            notificationService.createInAppNotification(email, "Feedback Submitted",
                    "You earned " + GamificationService.POINTS_FEEDBACK + " points for submitting feedback!");
            redirectAttributes.addFlashAttribute("successMessage", "Thank you! Feedback submitted successfully. You earned 5 points! ⭐");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/feedback/" + eventId;
    }

    /**
     * Admin: view event-wise feedback and ratings.
     */
    @GetMapping("/admin/feedback")
    public String adminFeedback(Model model) {
        List<Event> events = eventService.getAllEvents();
        Map<Long, Double> avgRatings = feedbackService.getAverageRatingsMap();
        List<Object[]> topRated = feedbackService.getTopRatedEvents();

        model.addAttribute("events", events);
        model.addAttribute("avgRatings", avgRatings);
        model.addAttribute("topRated", topRated);
        return "admin_feedback";
    }

    /**
     * Admin: view all feedback for a specific event.
     */
    @GetMapping("/admin/feedback/{eventId}")
    public String adminEventFeedback(@PathVariable Long eventId, Model model) {
        Event event = eventService.getEventById(eventId);
        List<Feedback> feedbackList = feedbackService.getFeedbackForEvent(eventId);
        Double avg = feedbackService.getAverageRating(eventId);
        model.addAttribute("event", event);
        model.addAttribute("feedbackList", feedbackList);
        model.addAttribute("averageRating", avg);
        return "admin_feedback_detail";
    }
}
