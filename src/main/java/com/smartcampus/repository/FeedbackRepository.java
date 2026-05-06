package com.smartcampus.repository;

import com.smartcampus.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByEventIdOrderBySubmittedAtDesc(Long eventId);

    Optional<Feedback> findByStudentEmailAndEventId(String email, Long eventId);

    boolean existsByStudentEmailAndEventId(String email, Long eventId);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.event.id = :eventId")
    Double findAverageRatingByEventId(@Param("eventId") Long eventId);

    @Query("SELECT f.event.id, AVG(f.rating) FROM Feedback f GROUP BY f.event.id ORDER BY AVG(f.rating) DESC")
    List<Object[]> findAverageRatingGroupedByEvent();

    @Query("SELECT f.event.name, AVG(f.rating) as avgRating, f.event.id FROM Feedback f GROUP BY f.event.id, f.event.name ORDER BY avgRating DESC")
    List<Object[]> findTopRatedEvents();
}
