package com.smartcampus.controller;

import com.smartcampus.entity.Event;
import com.smartcampus.service.EventService;
import com.smartcampus.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private EventService eventService;

    @Autowired
    private RegistrationService registrationService;

    @GetMapping("/events")
    public String listEvents(@RequestParam(required = false) String department,
                             @RequestParam(required = false) String type,
                             @RequestParam(required = false) LocalDate date,
                             Model model) {
        model.addAttribute("events", eventService.findEventsFiltered(department, type, date));
        model.addAttribute("regCounts", registrationService.getRegistrationCountsPerEvent());
        return "admin_events";
    }

    @GetMapping("/events/new")
    public String createEventForm(Model model) {
        model.addAttribute("event", new Event());
        return "admin_event_form";
    }

    @PostMapping("/events")
    public String saveEvent(@Valid @ModelAttribute("event") Event event,
                            BindingResult result) {
        if (result.hasErrors()) {
            return "admin_event_form";
        }
        eventService.saveEvent(event);
        return "redirect:/admin/events";
    }
    
    @GetMapping("/events/edit/{id}")
    public String editEventForm(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.getEventById(id));
        return "admin_event_form";
    }
    
    @GetMapping("/events/delete/{id}")
    public String deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return "redirect:/admin/events";
    }

    @GetMapping("/stats")
    public String viewStats(Model model) {
        model.addAttribute("stats", registrationService.getRegistrationStats());
        model.addAttribute("totalRegistrations", registrationService.getAllRegistrations().size());
        return "admin_stats";
    }
}
