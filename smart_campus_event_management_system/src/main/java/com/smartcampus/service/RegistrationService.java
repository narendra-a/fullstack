package com.smartcampus.service;

import com.smartcampus.entity.Event;
import com.smartcampus.entity.Registration;
import com.smartcampus.entity.Student;
import com.smartcampus.repository.EventRepository;
import com.smartcampus.repository.RegistrationRepository;
import com.smartcampus.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public void registerStudentForEvent(Long eventId, Student studentInput) {
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
    }

    public void cancelRegistration(Long eventId, String email) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        List<Registration> registrations = registrationRepository.findByStudentEmail(email);
        Registration target = registrations.stream()
                .filter(r -> r.getEvent().getId().equals(eventId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Registration not found."));

        registrationRepository.delete(target);
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

    /**
     * Returns a map of eventId -> registration count for the admin events table.
     */
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
}
