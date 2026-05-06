package com.smartcampus.service;

import com.smartcampus.entity.Event;
import com.smartcampus.entity.Registration;
import com.smartcampus.entity.Student;
import com.smartcampus.repository.EventRepository;
import com.smartcampus.repository.RegistrationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    @Lazy
    private NotificationService notificationService;

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<Event> findEventsFiltered(String department, String type, LocalDate date) {
        if (department != null && department.isEmpty()) department = null;
        if (type != null && type.isEmpty()) type = null;
        return eventRepository.findByFilters(department, type, date);
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new RuntimeException("Event not found"));
    }

    @Autowired
    private com.smartcampus.repository.StudentRepository studentRepository;

    public Event saveEvent(Event event) {
        boolean isNew = (event.getId() == null);
        Event saved = eventRepository.save(event);
        if (isNew) {
            // New Event Announcement
            List<com.smartcampus.entity.Student> allStudents = studentRepository.findAll();
            allStudents.forEach(student -> {
                try {
                    notificationService.sendNewEventAnnouncement(student, saved);
                } catch (Exception e) {
                    log.error("Failed to announce new event to {}: {}", student.getEmail(), e.getMessage());
                }
            });
        }
        return saved;
    }

    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }

    // ─────────────────────────────────────────────────────────────────
    // Scheduled: Send reminders 1 day before event at 8 AM daily
    // ─────────────────────────────────────────────────────────────────
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDayBeforeReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Event> events = eventRepository.findByEventDate(tomorrow);
        log.info("Checking day-before reminders: {} events tomorrow", events.size());
        for (Event event : events) {
            List<Registration> registrations = registrationRepository.findByEventIdWithStudent(event.getId());
            for (Registration reg : registrations) {
                Student student = reg.getStudent();
                try {
                    notificationService.sendEventReminder(student, event, "tomorrow");
                } catch (Exception e) {
                    log.error("Failed to send day-before reminder to {}: {}", student.getEmail(), e.getMessage());
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Scheduled: Send 1-hour reminders (runs every hour at :00)
    // ─────────────────────────────────────────────────────────────────
    @Scheduled(cron = "0 0 * * * *")
    public void sendHourBeforeReminders() {
        LocalDate today = LocalDate.now();
        List<Event> events = eventRepository.findByEventDate(today);
        // Would filter by time ± 1h in production; simplified here
        log.info("Hourly reminder check: {} events today", events.size());
    }
}
