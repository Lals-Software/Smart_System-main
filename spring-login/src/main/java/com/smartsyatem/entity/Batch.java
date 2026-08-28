package com.smartsyatem.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "batches")
public class Batch {
    @Id
    private String id; // e.g., "2023-2027"
    private String name; // e.g., "Batch 2023-2027"
    private Integer semester;
    private Integer courseCount;

    public Batch() {}

    public Batch(String id, String name, Integer semester, Integer courseCount) {
        this.id = id;
        this.name = name;
        this.semester = semester;
        this.courseCount = courseCount;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }
    public Integer getCourseCount() { return courseCount; }
    public void setCourseCount(Integer courseCount) { this.courseCount = courseCount; }
}