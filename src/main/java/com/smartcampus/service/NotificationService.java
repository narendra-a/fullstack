package com.smartcampus.service;

import com.smartcampus.entity.Notification;
import com.smartcampus.entity.Notification.NotificationType;
import com.smartcampus.entity.Event;
import com.smartcampus.entity.Student;
import com.smartcampus.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@smartcampus.com}")
    private String fromEmail;

    // ─────────────────────────────────────────────────────────────────
    // In-App Notifications
    // ─────────────────────────────────────────────────────────────────

    public void createInAppNotification(String email, String title, String message) {
        Notification notification = new Notification();
        notification.setStudentEmail(email);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(NotificationType.IN_APP);
        notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsForStudent(String email) {
        return notificationRepository.findByStudentEmailOrderByCreatedAtDesc(email);
    }

    public long getUnreadCount(String email) {
        return notificationRepository.countByStudentEmailAndIsReadFalse(email);
    }

    public void markAllRead(String email) {
        notificationRepository.markAllReadByEmail(email);
    }

    // ─────────────────────────────────────────────────────────────────
    // Email Notifications
    // ─────────────────────────────────────────────────────────────────

    public void sendRegistrationConfirmation(Student student, Event event) {
        String title = "Registration Confirmed: " + event.getName();
        String message = String.format(
            "Hi %s, you have successfully registered for '%s' on %s at %s.",
            student.getName(), event.getName(),
            event.getEventDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
            event.getLocation() != null ? event.getLocation() : "TBD"
        );

        // In-app
        createInAppNotification(student.getEmail(), title, message);

        // Email
        sendEmail(student.getEmail(), title, message);
    }

    public void sendEventReminder(Student student, Event event, String timeframe) {
        String title = "Reminder: " + event.getName() + " is " + timeframe;
        String message = String.format(
            "Hi %s, this is a reminder that '%s' is happening %s on %s at %s. Don't forget!",
            student.getName(), event.getName(), timeframe,
            event.getEventDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
            event.getLocation() != null ? event.getLocation() : "TBD"
        );

        createInAppNotification(student.getEmail(), title, message);
        sendEmail(student.getEmail(), title, message);
    }

    public void sendNewEventAnnouncement(Student student, Event event) {
        String title = "New Event: " + event.getName();
        String message = String.format(
            "Hi %s, a new event '%s' has been announced for %s. Check it out on Smart Campus!",
            student.getName(), event.getName(),
            event.getEventDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
        );
        createInAppNotification(student.getEmail(), title, message);
        sendEmail(student.getEmail(), title, message);
    }

    private void sendEmail(String to, String subject, String body) {
        if (mailSender == null) {
            log.warn("JavaMailSender not configured. Skipping email to {}", to);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("[Smart Campus] " + subject);
            message.setText(body + "\n\nThank you,\nSmart Campus Team");
            mailSender.send(message);
            log.info("Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
