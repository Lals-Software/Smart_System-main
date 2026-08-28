package com.smartsyatem.repository;

import com.smartsyatem.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findBySemester(Long semester);
    Optional<Course> findByName(String name);
}