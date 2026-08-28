package com.smartsyatem.config;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.smartsyatem.entity.Batch;
import com.smartsyatem.entity.Course;
import com.smartsyatem.entity.SemesterSettings;
import com.smartsyatem.entity.UserAccount;
import com.smartsyatem.repository.BatchRepository;
import com.smartsyatem.repository.CourseRepository;
import com.smartsyatem.repository.SemesterSettingsRepository;
import com.smartsyatem.repository.UserAccountRepository;

@Configuration
public class DataInitializer {

    @SuppressWarnings("null")
	@Bean
    public CommandLineRunner initializeData(UserAccountRepository repository, BatchRepository batchRepository, SemesterSettingsRepository settingsRepository, CourseRepository courseRepository) {
        return args -> {
            if (repository.findByRoleAndUsername("admin", "admin").isEmpty()) {
                repository.save(createUser("staff", "sarah.smith@college.edu", "password", "Dr. Sarah Smith"));
                repository.save(createUser("admin", "admin", "admin", "Administrator"));
            }
            
            if (batchRepository.count() == 0) {
                batchRepository.save(new Batch("2023-2027", "Batch 2023-2027", 1, 0));
                batchRepository.save(new Batch("2024-2028", "Batch 2024-2028", 1, 0));
                batchRepository.save(new Batch("2025-2029", "Batch 2025-2029", 1, 0));
            }

            if (settingsRepository.count() == 0) {
                SemesterSettings defaultSettings = new SemesterSettings();
                defaultSettings.setAcademicYear("2025-2026");
                defaultSettings.setSemesterType("odd");
                defaultSettings.setStartDate(LocalDate.parse("2025-08-01"));
                defaultSettings.setEndDate(LocalDate.parse("2025-12-15"));
                settingsRepository.save(defaultSettings);
            }

            // Add some default courses for testing student enrollment
            if (courseRepository.count() == 0) {
                courseRepository.save(new Course("CS101", "Data Structures", "DS", "Core", 4, 3L));
                courseRepository.save(new Course("CS102", "Algorithms", "ALGO", "Core", 4, 4L));
                courseRepository.save(new Course("MA101", "Calculus I", "MAT1", "Core", 3, 1L));
                courseRepository.save(new Course("PH101", "Physics I", "PHY1", "Core", 3, 1L));
            }

            if (repository.findByRoleAndUsername("student", "91762214001").isEmpty()) {
                UserAccount student = createUser("student", "91762214001", "password", "John Doe");
                student.setBatchId("2023-2027");
                student.getEnrolledCourses().addAll(courseRepository.findAll());
                repository.save(student);
            }
        };
    }

    private UserAccount createUser(String role, String username, String password, String name) {
        UserAccount account = new UserAccount();
        account.setRole(role);
        account.setUsername(username);
        account.setIdentifier(username);
        account.setPassword(password);
        account.setName(name);
        account.setIsApproved(true); // Seeded accounts should be pre-approved
        if ("staff".equals(role)) account.setEmail(username);
        return account;
    }
}
