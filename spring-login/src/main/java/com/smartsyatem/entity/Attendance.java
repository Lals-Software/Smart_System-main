package com.smartsyatem.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "attendance")
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attendance_date")
    private LocalDate date;

    @Column(name = "attendance_period")
    private Integer period;

    @Column(name = "attendance_status")
    private String status; // present, absent, late

    @ManyToOne
    @JoinColumn(name = "student_id")
    private UserAccount student;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    public Attendance() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public Integer getPeriod() { return period; }
    public void setPeriod(Integer period) { this.period = period; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public UserAccount getStudent() { return student; }
    public void setStudent(UserAccount student) { this.student = student; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
}