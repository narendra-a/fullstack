package com.smartcampus.controller;

import com.smartcampus.entity.Event;
import com.smartcampus.entity.Registration;
import com.smartcampus.entity.Student;
import com.smartcampus.service.EventService;
import com.smartcampus.service.FeedbackService;
import com.smartcampus.service.GamificationService;
import com.smartcampus.service.NotificationService;
import com.smartcampus.service.RegistrationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class StudentController {

    @Autowired
    private EventService eventService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private GamificationService gamificationService;

    @Autowired
    private FeedbackService feedbackService;

    private static final String SESSION_STUDENT_NAME  = "studentName";
    private static final String SESSION_STUDENT_EMAIL = "studentEmail";

    // ─────────────────────────────────────────────────────────────────────
    // HOME PAGE
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        List<Event> events = eventService.getAllEvents();
        model.addAttribute("events", events);

        String email = (String) session.getAttribute(SESSION_STUDENT_EMAIL);
        if (email != null) {
            model.addAttribute("registeredEventIds",
                    registrationService.getRegisteredEventIds(email));
        } else {
            model.addAttribute("registeredEventIds", Collections.emptyList());
        }

        model.addAttribute("regCounts", registrationService.getRegistrationCountsPerEvent());
        model.addAttribute("avgRatings", feedbackService.getAverageRatingsMap());
        model.addAttribute("topRatedEvents", feedbackService.getTopRatedEventsList());
        return "index";
    }

    // ─────────────────────────────────────────────────────────────────────
    // STUDENT LOGIN
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/student/login")
    public String studentLoginForm(HttpSession session) {
        if (session.getAttribute(SESSION_STUDENT_NAME) != null) {
            return "redirect:/student/dashboard";
        }
        return "student_login";
    }

    @PostMapping("/student/login")
    public String studentLoginSubmit(
            @RequestParam("studentName")  String name,
            @RequestParam("studentEmail") String email,
            HttpSession session,
            Model model) {

        if (name == null || name.trim().isEmpty()) {
            model.addAttribute("error", "Please enter your full name.");
            return "student_login";
        }
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            model.addAttribute("error", "Please enter a valid email address.");
            return "student_login";
        }

        session.setAttribute(SESSION_STUDENT_NAME,  name.trim());
        session.setAttribute(SESSION_STUDENT_EMAIL, email.trim().toLowerCase());
        return "redirect:/student/dashboard";
    }

    @GetMapping("/student/logout")
    public String studentLogout(HttpSession session) {
        session.removeAttribute(SESSION_STUDENT_NAME);
        session.removeAttribute(SESSION_STUDENT_EMAIL);
        return "redirect:/";
    }

    // ─────────────────────────────────────────────────────────────────────
    // STUDENT DASHBOARD
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/student/dashboard")
    public String studentDashboard(
            @RequestParam(value = "type", required = false) String type,
            HttpSession session,
            Model model) {

        String studentName  = (String) session.getAttribute(SESSION_STUDENT_NAME);
        String studentEmail = (String) session.getAttribute(SESSION_STUDENT_EMAIL);

        if (studentName == null) return "redirect:/student/login";

        List<Event> allEvents = eventService.getAllEvents();
        List<Event> filteredEvents = (type != null && !type.isEmpty())
                ? allEvents.stream().filter(e -> type.equals(e.getType())).collect(Collectors.toList())
                : allEvents;

        long workshopsCount = allEvents.stream()
                .filter(e -> "Workshop".equals(e.getType()) || "Seminar".equals(e.getType())).count();
        long hackathonCount = allEvents.stream()
                .filter(e -> "Hackathon".equals(e.getType()) || "Cultural".equals(e.getType())).count();
        long deptCount = allEvents.stream()
                .map(Event::getDepartment).filter(d -> d != null && !d.isBlank()).distinct().count();

        List<Registration> myRegistrations = registrationService.getRegistrationsByEmail(studentEmail);
        List<Long> registeredEventIds = registrationService.getRegisteredEventIds(studentEmail);
        long unreadCount = notificationService.getUnreadCount(studentEmail);

        model.addAttribute("events",             filteredEvents);
        model.addAttribute("totalEvents",        allEvents.size());
        model.addAttribute("workshopsCount",     workshopsCount);
        model.addAttribute("hackathonCount",     hackathonCount);
        model.addAttribute("deptCount",          deptCount);
        model.addAttribute("selectedType",       type);
        model.addAttribute("studentName",        studentName);
        model.addAttribute("studentEmail",       studentEmail);
        model.addAttribute("myRegistrations",    myRegistrations);
        model.addAttribute("registeredEventIds", registeredEventIds);
        model.addAttribute("regCounts",          registrationService.getRegistrationCountsPerEvent());
        model.addAttribute("avgRatings",         feedbackService.getAverageRatingsMap());
        model.addAttribute("unreadCount",        unreadCount);
        model.addAttribute("studentPoints",
                gamificationService.getStudentPoints(studentEmail).orElse(null));

        return "student_dashboard";
    }

    // ─────────────────────────────────────────────────────────────────────
    // EVENT DETAIL PAGE
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/events/{id}")
    public String eventDetail(@PathVariable Long id, HttpSession session, Model model) {
        Event event = eventService.getEventById(id);
        model.addAttribute("event", event);

        long regCount = registrationService.getRegistrationCountForEvent(id);
        model.addAttribute("registrationCount", regCount);
        model.addAttribute("averageRating", feedbackService.getAverageRating(id));
        model.addAttribute("feedbackList", feedbackService.getFeedbackForEvent(id));

        String email = (String) session.getAttribute(SESSION_STUDENT_EMAIL);
        if (email != null) {
            List<Long> registeredIds = registrationService.getRegisteredEventIds(email);
            model.addAttribute("isRegistered", registeredIds.contains(id));
            model.addAttribute("hasFeedback", feedbackService.hasUserSubmittedFeedback(email, id));
        } else {
            model.addAttribute("isRegistered", false);
            model.addAttribute("hasFeedback", false);
        }
        model.addAttribute("studentEmail", email);
        return "event_detail";
    }

    // ─────────────────────────────────────────────────────────────────────
    // EVENT REGISTRATION
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/events/register/{id}")
    public String showRegistrationForm(@PathVariable Long id,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {

        String sessionName  = (String) session.getAttribute(SESSION_STUDENT_NAME);
        String sessionEmail = (String) session.getAttribute(SESSION_STUDENT_EMAIL);

        if (sessionName != null && sessionEmail != null) {
            try {
                Student student = new Student();
                student.setName(sessionName);
                student.setEmail(sessionEmail);
                Registration reg = registrationService.registerStudentForEvent(id, student);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Registration successful! Here's your QR code for check-in. 🎉");
                return "redirect:/student/qr/" + reg.getId();
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                return "redirect:/student/dashboard";
            }
        }

        model.addAttribute("event",   eventService.getEventById(id));
        model.addAttribute("student", new Student());
        model.addAttribute("registrationCount", registrationService.getRegistrationCountForEvent(id));
        return "register";
    }

    @PostMapping("/student/register")
    public String registerForEvent(
            @RequestParam("eventId") Long eventId,
            @ModelAttribute("student") Student student,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            Registration reg = registrationService.registerStudentForEvent(eventId, student);
            // Set session so the QR page can verify access
            session.setAttribute(SESSION_STUDENT_NAME, student.getName());
            session.setAttribute(SESSION_STUDENT_EMAIL, student.getEmail().toLowerCase());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Successfully registered! Here's your QR code for check-in. 🎉");
            return "redirect:/student/qr/" + reg.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/events/register/" + eventId;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // CANCEL REGISTRATION
    // ─────────────────────────────────────────────────────────────────────
    @PostMapping("/student/cancel-registration")
    public String cancelRegistration(
            @RequestParam("eventId") Long eventId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute(SESSION_STUDENT_EMAIL);
        if (email == null) return "redirect:/student/login";

        try {
            registrationService.cancelRegistration(eventId, email);
            redirectAttributes.addFlashAttribute("successMessage", "Registration cancelled successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/student/dashboard";
    }

    // ─────────────────────────────────────────────────────────────────────
    // MY REGISTRATIONS
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/student/my-events")
    public String myEvents(
            @RequestParam(value = "email", required = false) String email,
            HttpSession session,
            Model model) {

        String sessionEmail = (String) session.getAttribute(SESSION_STUDENT_EMAIL);
        String finalEmail = (email != null && !email.isEmpty()) ? email : sessionEmail;

        if (finalEmail != null && !finalEmail.isEmpty()) {
            model.addAttribute("registrations", registrationService.getRegistrationsByEmail(finalEmail));
            model.addAttribute("displayEmail", finalEmail);
            model.addAttribute("studentName", session.getAttribute(SESSION_STUDENT_NAME));
        }
        return "my_events";
    }
}
