package com.smartsyatem.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "timetable_entries")
public class TimetableEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String day;
    private Integer period;
    private String subject;
    private String type;
    private String staffName;
    private String year; // e.g., "1", "2", "3", "4"

    public TimetableEntry() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }
    public Integer getPeriod() { return period; }
    public void setPeriod(Integer period) { this.period = period; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
}