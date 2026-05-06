package com.smartcampus.controller;

import com.smartcampus.entity.Event;
import com.smartcampus.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.format.DateTimeFormatter;

@Controller
public class CalendarController {

    @Autowired
    private EventService eventService;

    /**
     * Download .ics calendar file for a given event.
     */
    @GetMapping("/events/{id}/calendar.ics")
    public ResponseEntity<byte[]> downloadIcs(@PathVariable Long id) {
        Event event = eventService.getEventById(id);

        String ics = buildIcsContent(event);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("text/calendar"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=\"" + sanitizeFilename(event.getName()) + ".ics\"");

        return ResponseEntity.ok()
                .headers(headers)
                .body(ics.getBytes());
    }

    private String buildIcsContent(Event event) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

        String dtStart;
        String dtEnd;

        if (event.getEventTime() != null) {
            dtStart = event.getEventDate().atTime(event.getEventTime()).format(dateTimeFormatter);
            dtEnd   = event.getEventDate().atTime(event.getEventTime().plusHours(2)).format(dateTimeFormatter);
        } else {
            dtStart = event.getEventDate().format(dateFormatter);
            dtEnd   = event.getEventDate().plusDays(1).format(dateFormatter);
        }

        String uid = "event-" + event.getId() + "@smartcampus.local";
        String location = event.getLocation() != null ? event.getLocation() : "Smart Campus";
        String summary = escape(event.getName());
        String description = escape(event.getDescription());

        return "BEGIN:VCALENDAR\r\n" +
               "VERSION:2.0\r\n" +
               "PRODID:-//Smart Campus//Event Management//EN\r\n" +
               "CALSCALE:GREGORIAN\r\n" +
               "METHOD:PUBLISH\r\n" +
               "BEGIN:VEVENT\r\n" +
               "UID:" + uid + "\r\n" +
               (event.getEventTime() != null
                   ? "DTSTART:" + dtStart + "\r\n" + "DTEND:" + dtEnd + "\r\n"
                   : "DTSTART;VALUE=DATE:" + dtStart + "\r\n" + "DTEND;VALUE=DATE:" + dtEnd + "\r\n") +
               "SUMMARY:" + summary + "\r\n" +
               "DESCRIPTION:" + description + "\r\n" +
               "LOCATION:" + escape(location) + "\r\n" +
               "STATUS:CONFIRMED\r\n" +
               "BEGIN:VALARM\r\n" +
               "TRIGGER:-PT1H\r\n" +
               "ACTION:DISPLAY\r\n" +
               "DESCRIPTION:Reminder: " + summary + "\r\n" +
               "END:VALARM\r\n" +
               "END:VEVENT\r\n" +
               "END:VCALENDAR\r\n";
    }

    private String escape(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace(";", "\\;")
                   .replace(",", "\\,")
                   .replace("\n", "\\n");
    }

    private String sanitizeFilename(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
}
