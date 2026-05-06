package com.smartcampus.repository;

import com.smartcampus.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByStudentEmailOrderByCreatedAtDesc(String email);

    long countByStudentEmailAndIsReadFalse(String email);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.studentEmail = :email AND n.isRead = false")
    void markAllReadByEmail(@Param("email") String email);
}
