package com.smartcampus.repository;

import com.smartcampus.entity.StudentPoints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudentPointsRepository extends JpaRepository<StudentPoints, Long> {

    Optional<StudentPoints> findByStudentEmail(String email);

    Optional<StudentPoints> findByStudentId(Long studentId);

    @Query("SELECT sp FROM StudentPoints sp JOIN FETCH sp.student ORDER BY sp.totalPoints DESC")
    List<StudentPoints> findLeaderboard();
}
