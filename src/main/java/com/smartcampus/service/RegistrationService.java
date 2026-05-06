package com.smartcampus.service;

import com.smartcampus.entity.Event;
import com.smartcampus.entity.Registration;
import com.smartcampus.entity.Student;
import com.smartcampus.repository.EventRepository;
import com.smartcampus.repository.RegistrationRepository;
import com.smartcampus.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RegistrationService {

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    @Lazy
    private QrCodeService qrCodeService;

    @Autowired
    @Lazy
    private GamificationService gamificationService;

    @Autowired
    @Lazy
    private NotificationService notificationService;

    public Registration registerStudentForEvent(Long eventId, Student studentInput) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (registrationRepository.existsByEventAndStudentEmail(event, studentInput.getEmail())) {
            throw new RuntimeException("You are already registered for this event.");
        }

        // Capacity check
        if (event.getCapacity() != null) {
            long currentCount = registrationRepository.countByEvent(event);
            if (currentCount >= event.getCapacity()) {
                throw new RuntimeException("This event is full. No more seats available.");
            }
        }

        Student student = studentRepository.findByEmail(studentInput.getEmail())
                .orElseGet(() -> studentRepository.save(studentInput));

        Registration registration = new Registration();
        registration.setEvent(event);
        registration.setStudent(student);
        registrationRepository.save(registration);

        // Generate & store QR code
        String qrPayload = qrCodeService.buildPayload(registration);
        registration.setQrCodeData(qrPayload);
        registrationRepository.save(registration);

        // Award 10 points
        try {
            gamificationService.awardPoints(student.getEmail(), "REGISTER");
        } catch (Exception e) {
            // Non-critical
        }

        // Send registration confirmation notification
        try {
            notificationService.sendRegistrationConfirmation(student, event);
        } catch (Exception e) {
            // Non-critical
        }

        return registration;
    }

    public void cancelRegistration(Long eventId, String email) {
        List<Registration> registrations = registrationRepository.findByStudentEmail(email);
        Registration target = registrations.stream()
                .filter(r -> r.getEvent().getId().equals(eventId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Registration not found."));
        registrationRepository.delete(target);
    }

    /**
     * Process QR check-in scan. Returns true if successfully checked in.
     */
    public String checkIn(String qrPayload) {
        Long registrationId = qrCodeService.extractRegistrationId(qrPayload);
        if (registrationId < 0) {
            return "INVALID: Unrecognized QR code format.";
        }

        Optional<Registration> opt = registrationRepository.findById(registrationId);
        if (opt.isEmpty()) {
            return "NOT_FOUND: No registration found for this QR code.";
        }

        Registration reg = opt.get();
        if (reg.isAttended()) {
            return "DUPLICATE: " + reg.getStudent().getName() + " has already checked in at "
                    + reg.getCheckedInAt().toString().replace("T", " ").substring(0, 16);
        }

        reg.setAttended(true);
        reg.setCheckedInAt(LocalDateTime.now());
        registrationRepository.save(reg);

        // Award 20 points
        try {
            gamificationService.awardPoints(reg.getStudent().getEmail(), "ATTEND");
        } catch (Exception e) {
            // Non-critical
        }

        return "SUCCESS: " + reg.getStudent().getName() + " checked in for " + reg.getEvent().getName();
    }

    public List<Registration> getRegistrationsByEmail(String email) {
        return registrationRepository.findByStudentEmail(email);
    }

    public List<Object[]> getRegistrationStats() {
        return registrationRepository.countRegistrationsPerEvent();
    }

    public List<Registration> getAllRegistrations() {
        return registrationRepository.findAll();
    }

    public List<Long> getRegisteredEventIds(String email) {
        return registrationRepository.findRegisteredEventIdsByEmail(email);
    }

    public Map<Long, Long> getRegistrationCountsPerEvent() {
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : registrationRepository.countRegistrationsGroupedByEventId()) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return counts;
    }

    public long getRegistrationCountForEvent(Long eventId) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) return 0;
        return registrationRepository.countByEvent(event);
    }

    public Optional<Registration> getRegistrationById(Long id) {
        return registrationRepository.findById(id);
    }

    public List<Registration> getRegistrationsByEventId(Long eventId) {
        return registrationRepository.findByEventIdWithStudent(eventId);
    }
}
