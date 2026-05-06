package com.smartcampus.repository;

import com.smartcampus.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE " +
           "(:department IS NULL OR e.department = :department) AND " +
           "(:type IS NULL OR e.type = :type) AND " +
           "(:date IS NULL OR e.eventDate >= :date)")
    List<Event> findByFilters(@Param("department") String department,
                              @Param("type") String type,
                              @Param("date") LocalDate date);

    List<Event> findByEventDate(LocalDate eventDate);

    List<Event> findByEventDateBetween(LocalDate start, LocalDate end);
}
