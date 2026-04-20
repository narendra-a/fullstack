package com.smartcampus.repository;

import com.smartcampus.entity.Registration;
import com.smartcampus.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByStudentEmail(String email);
    boolean existsByEventAndStudentEmail(Event event, String email);

    long countByEvent(Event event);

    @Query("SELECT r.event.id FROM Registration r WHERE r.student.email = :email")
    List<Long> findRegisteredEventIdsByEmail(@Param("email") String email);

    // Aggregate function for stats
    @Query("SELECT r.event.name, COUNT(r) as cnt FROM Registration r GROUP BY r.event.name ORDER BY cnt DESC")
    List<Object[]> countRegistrationsPerEvent();

    // Count per event for admin table
    @Query("SELECT r.event.id, COUNT(r) FROM Registration r GROUP BY r.event.id")
    List<Object[]> countRegistrationsGroupedByEventId();
}
