package com.smartcampus.service;

import com.smartcampus.entity.Feedback;
import com.smartcampus.entity.Event;
import com.smartcampus.entity.Student;
import com.smartcampus.repository.EventRepository;
import com.smartcampus.repository.FeedbackRepository;
import com.smartcampus.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EventRepository eventRepository;

    public void submitFeedback(String email, Long eventId, int rating, String comment) {
        if (feedbackRepository.existsByStudentEmailAndEventId(email, eventId)) {
            throw new RuntimeException("You have already submitted feedback for this event.");
        }

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Only allow feedback after event date has passed
        if (event.getEventDate().isAfter(java.time.LocalDate.now())) {
            throw new RuntimeException("Feedback can only be submitted after the event is completed.");
        }

        Feedback feedback = new Feedback();
        feedback.setStudent(student);
        feedback.setEvent(event);
        feedback.setRating(rating);
        feedback.setComment(comment);
        feedbackRepository.save(feedback);
    }

    public List<Feedback> getFeedbackForEvent(Long eventId) {
        return feedbackRepository.findByEventIdOrderBySubmittedAtDesc(eventId);
    }

    public Double getAverageRating(Long eventId) {
        Double avg = feedbackRepository.findAverageRatingByEventId(eventId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : null;
    }

    public boolean hasUserSubmittedFeedback(String email, Long eventId) {
        return feedbackRepository.existsByStudentEmailAndEventId(email, eventId);
    }

    /**
     * Returns a map of eventId -> average rating for all events that have feedback.
     */
    public Map<Long, Double> getAverageRatingsMap() {
        Map<Long, Double> map = new LinkedHashMap<>();
        for (Object[] row : feedbackRepository.findAverageRatingGroupedByEvent()) {
            Long eventId = (Long) row[0];
            Double avg = (Double) row[1];
            map.put(eventId, Math.round(avg * 10.0) / 10.0);
        }
        return map;
    }

    public List<Object[]> getTopRatedEvents() {
        return feedbackRepository.findTopRatedEvents();
    }

    /**
     * Get a list of the top 3 rated events with their details.
     */
    public List<Event> getTopRatedEventsList() {
        List<Object[]> top = feedbackRepository.findTopRatedEvents();
        return top.stream()
                .limit(3)
                .map(row -> eventRepository.findById((Long) row[2]).orElse(null))
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());
    }
}
