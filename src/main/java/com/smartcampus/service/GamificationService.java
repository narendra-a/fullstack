package com.smartcampus.service;

import com.smartcampus.entity.Student;
import com.smartcampus.entity.StudentPoints;
import com.smartcampus.repository.StudentPointsRepository;
import com.smartcampus.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GamificationService {

    public static final int POINTS_REGISTER  = 10;
    public static final int POINTS_ATTEND    = 20;
    public static final int POINTS_FEEDBACK  = 5;

    public static final String BADGE_ACTIVE_PARTICIPANT = "Active Participant";
    public static final String BADGE_TOP_CONTRIBUTOR    = "Top Contributor";
    public static final String BADGE_EVENT_ENTHUSIAST   = "Event Enthusiast";

    @Autowired
    private StudentPointsRepository studentPointsRepository;

    @Autowired
    private StudentRepository studentRepository;

    public StudentPoints getOrCreate(String email) {
        return studentPointsRepository.findByStudentEmail(email).orElseGet(() -> {
            Student student = studentRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Student not found: " + email));
            StudentPoints sp = new StudentPoints();
            sp.setStudent(student);
            sp.setTotalPoints(0);
            sp.setBadges("");
            return studentPointsRepository.save(sp);
        });
    }

    public void awardPoints(String email, String action) {
        int points = switch (action.toUpperCase()) {
            case "REGISTER" -> POINTS_REGISTER;
            case "ATTEND"   -> POINTS_ATTEND;
            case "FEEDBACK" -> POINTS_FEEDBACK;
            default         -> 0;
        };
        if (points == 0) return;

        StudentPoints sp = getOrCreate(email);
        sp.setTotalPoints(sp.getTotalPoints() + points);
        checkAndAwardBadges(sp);
        studentPointsRepository.save(sp);
    }

    public void checkAndAwardBadges(StudentPoints sp) {
        List<String> badges = new ArrayList<>(
                sp.getBadges() == null || sp.getBadges().isBlank()
                        ? List.of()
                        : Arrays.asList(sp.getBadges().split(","))
        );

        int pts = sp.getTotalPoints();

        if (pts >= 10 && !badges.contains(BADGE_ACTIVE_PARTICIPANT)) {
            badges.add(BADGE_ACTIVE_PARTICIPANT);
        }
        if (pts >= 50 && !badges.contains(BADGE_EVENT_ENTHUSIAST)) {
            badges.add(BADGE_EVENT_ENTHUSIAST);
        }
        if (pts >= 100 && !badges.contains(BADGE_TOP_CONTRIBUTOR)) {
            badges.add(BADGE_TOP_CONTRIBUTOR);
        }

        sp.setBadges(String.join(",", badges));
    }

    public List<StudentPoints> getLeaderboard() {
        return studentPointsRepository.findLeaderboard();
    }

    public Optional<StudentPoints> getStudentPoints(String email) {
        return studentPointsRepository.findByStudentEmail(email);
    }

    public int getRank(String email) {
        List<StudentPoints> leaderboard = getLeaderboard();
        for (int i = 0; i < leaderboard.size(); i++) {
            if (leaderboard.get(i).getStudent().getEmail().equalsIgnoreCase(email)) {
                return i + 1;
            }
        }
        return -1;
    }
}
