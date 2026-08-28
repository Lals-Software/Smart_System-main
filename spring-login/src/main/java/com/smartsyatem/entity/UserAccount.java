package com.smartsyatem.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_accounts")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username; // This stores regNo for students or email for staff
    private String password;
    private String role;     // student, staff, admin
    private String name;
    private String email;
    private String phone;
    private String dob;
    private String identifier;
    private String batchId;
    private Boolean isApproved = true;

    // For Staff: Teaching assignments
    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<TeachingAssignment> teachingAssignments = new ArrayList<>();

    // For Students: Enrolled courses
    @ManyToMany
    @JoinTable(name = "student_enrolled_courses",
               joinColumns = @JoinColumn(name = "student_id"),
               inverseJoinColumns = @JoinColumn(name = "course_id"))
    private final List<Course> enrolledCourses = new ArrayList<>();

    public UserAccount() {}

    public UserAccount(String username, String password, String role, String name) {
        this.username = username;
        this.identifier = username;
        this.password = password;
        this.role = role;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { 
        this.identifier = identifier;
        if (this.username == null) this.username = identifier;
    }

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public Boolean getIsApproved() { return isApproved; }
    public void setIsApproved(Boolean isApproved) { this.isApproved = isApproved; }

    // New getters/setters for email, teachingAssignments, enrolledCourses
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public List<TeachingAssignment> getTeachingAssignments() { return teachingAssignments; }
    public void setTeachingAssignments(List<TeachingAssignment> teachingAssignments) {
        // Clear existing assignments to handle removals
        this.teachingAssignments.forEach(assignment -> assignment.setStaff(null)); // Break old links
        this.teachingAssignments.clear();
        if (teachingAssignments != null) {
            teachingAssignments.forEach(assignment -> assignment.setStaff(this)); // Set new link
            this.teachingAssignments.addAll(teachingAssignments);
        }
    }

    public List<Course> getEnrolledCourses() { return enrolledCourses; }
    public void setEnrolledCourses(List<Course> enrolledCourses) {
        this.enrolledCourses.clear();
        if (enrolledCourses != null) {
            this.enrolledCourses.addAll(enrolledCourses);
        }
    }
}