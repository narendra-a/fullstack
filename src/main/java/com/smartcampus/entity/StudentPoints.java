package com.smartcampus.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "student_points")
public class StudentPoints {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

    private int totalPoints = 0;

    @Column(length = 500)
    private String badges = "";

    public StudentPoints() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }
    public String getBadges() { return badges; }
    public void setBadges(String badges) { this.badges = badges; }
}
