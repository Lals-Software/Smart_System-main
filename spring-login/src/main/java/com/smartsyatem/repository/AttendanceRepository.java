package com.smartsyatem.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartsyatem.entity.Attendance;
import com.smartsyatem.entity.UserAccount;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByStudent(UserAccount student);
    Optional<Attendance> findByStudentAndDateAndPeriod(UserAccount student, LocalDate date, Integer period);
    long countByStudentAndCourseAndStatusIn(UserAccount student, com.smartsyatem.entity.Course course, List<String> statuses);
    long countByStudentAndCourse(UserAccount student, com.smartsyatem.entity.Course course);
}