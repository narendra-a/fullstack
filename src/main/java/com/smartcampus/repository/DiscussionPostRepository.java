package com.smartcampus.repository;

import com.smartcampus.entity.DiscussionPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DiscussionPostRepository extends JpaRepository<DiscussionPost, Long> {

    @Query("SELECT d FROM DiscussionPost d WHERE d.event.id = :eventId AND d.parentPost IS NULL AND d.deleted = false ORDER BY d.createdAt ASC")
    List<DiscussionPost> findTopLevelPostsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT d FROM DiscussionPost d WHERE d.parentPost.id = :parentId AND d.deleted = false ORDER BY d.createdAt ASC")
    List<DiscussionPost> findRepliesByParentId(@Param("parentId") Long parentId);

    long countByEventIdAndDeletedFalse(Long eventId);
}
