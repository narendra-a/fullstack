package com.smartcampus.service;

import com.smartcampus.entity.Event;
import com.smartcampus.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<Event> findEventsFiltered(String department, String type, LocalDate date) {
        // Handle empty strings
        if (department != null && department.isEmpty()) department = null;
        if (type != null && type.isEmpty()) type = null;
        
        return eventRepository.findByFilters(department, type, date);
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new RuntimeException("Event not found"));
    }

    public Event saveEvent(Event event) {
        return eventRepository.save(event);
    }

    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }
}
