package com.smartcampus.service;

import com.smartcampus.entity.DiscussionPost;
import com.smartcampus.entity.Event;
import com.smartcampus.entity.Student;
import com.smartcampus.repository.DiscussionPostRepository;
import com.smartcampus.repository.EventRepository;
import com.smartcampus.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscussionService {

    @Autowired
    private DiscussionPostRepository discussionPostRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private StudentRepository studentRepository;

    public List<DiscussionPost> getThreadsForEvent(Long eventId) {
        List<DiscussionPost> posts = discussionPostRepository.findTopLevelPostsByEventId(eventId);
        // Eagerly load replies for each top-level post
        for (DiscussionPost post : posts) {
            post.setReplies(discussionPostRepository.findRepliesByParentId(post.getId()));
        }
        return posts;
    }

    public DiscussionPost addPost(String email, String name, Long eventId, String content, Long parentId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        DiscussionPost post = new DiscussionPost();
        post.setEvent(event);
        post.setContent(content.trim());
        post.setAuthorEmail(email);
        post.setAuthorName(name);

        // Link student if they exist in DB
        studentRepository.findByEmail(email).ifPresent(post::setStudent);

        if (parentId != null && parentId > 0) {
            DiscussionPost parent = discussionPostRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Parent post not found"));
            post.setParentPost(parent);
        }

        return discussionPostRepository.save(post);
    }

    public void deletePost(Long postId) {
        DiscussionPost post = discussionPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setDeleted(true);
        post.setContent("[This message has been removed by the moderator.]");
        discussionPostRepository.save(post);
    }

    public long getPostCountForEvent(Long eventId) {
        return discussionPostRepository.countByEventIdAndDeletedFalse(eventId);
    }
}
