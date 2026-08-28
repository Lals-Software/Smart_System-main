package com.smartsyatem.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private String name;
    private String shortName; // Abbreviation for display (e.g., ES&IoT)
    private String type; // e.g., "Mandatory", "Core", "Professional Elective"
    private Integer credits;
    private Long semester; // The semester this course is typically offered

    public Course() {}

    public Course(String code, String name, String shortName, String type, Integer credits, Long semester) {
        this.code = code;
        this.name = name;
        this.shortName = shortName;
        this.type = type;
        this.credits = credits;
        this.semester = semester;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }
    public Long getSemester() { return semester; }
    public void setSemester(Long semester) { this.semester = semester; }
}