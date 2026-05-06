package com.smartcampus.controller;

import com.smartcampus.entity.DiscussionPost;
import com.smartcampus.entity.Event;
import com.smartcampus.service.DiscussionService;
import com.smartcampus.service.EventService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class DiscussionController {

    @Autowired
    private DiscussionService discussionService;

    @Autowired
    private EventService eventService;

    @GetMapping("/events/{id}/discussion")
    public String viewDiscussion(@PathVariable Long id,
                                  HttpSession session,
                                  Model model) {
        Event event = eventService.getEventById(id);
        List<DiscussionPost> threads = discussionService.getThreadsForEvent(id);

        model.addAttribute("event", event);
        model.addAttribute("threads", threads);
        model.addAttribute("postCount", discussionService.getPostCountForEvent(id));
        model.addAttribute("studentName", session.getAttribute("studentName"));
        model.addAttribute("studentEmail", session.getAttribute("studentEmail"));
        return "event_discussion";
    }

    @PostMapping("/events/{id}/discussion")
    public String addPost(@PathVariable Long id,
                          @RequestParam String content,
                          @RequestParam(required = false) Long parentId,
                          HttpSession session,
                          Authentication auth,
                          RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("studentEmail");
        String name  = (String) session.getAttribute("studentName");

        // Also support admin posting
        if (email == null && auth != null) {
            email = auth.getName();
            name  = "Admin";
        }

        if (email == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to post.");
            return "redirect:/events/" + id + "/discussion";
        }

        if (content == null || content.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Post content cannot be empty.");
            return "redirect:/events/" + id + "/discussion";
        }

        try {
            discussionService.addPost(email, name, id, content, parentId);
            redirectAttributes.addFlashAttribute("successMessage", "Your post has been added.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/events/" + id + "/discussion";
    }

    @PostMapping("/admin/discussion/{postId}/delete")
    public String deletePost(@PathVariable Long postId,
                             @RequestParam Long eventId,
                             RedirectAttributes redirectAttributes) {
        try {
            discussionService.deletePost(postId);
            redirectAttributes.addFlashAttribute("successMessage", "Post removed by moderator.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/events/" + eventId + "/discussion";
    }
}
