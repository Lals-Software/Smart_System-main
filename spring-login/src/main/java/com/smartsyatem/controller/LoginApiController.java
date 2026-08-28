package com.smartsyatem.controller;


import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartsyatem.entity.Attendance;
import com.smartsyatem.entity.Batch;
import com.smartsyatem.entity.Course;
import com.smartsyatem.entity.SemesterSettings;
import com.smartsyatem.entity.TeachingAssignment;
import com.smartsyatem.entity.TimetableEntry;
import com.smartsyatem.entity.UserAccount;
import com.smartsyatem.model.LoginForm;
import com.smartsyatem.repository.AttendanceRepository;
import com.smartsyatem.repository.BatchRepository;
import com.smartsyatem.repository.CourseRepository;
import com.smartsyatem.repository.SemesterSettingsRepository;
import com.smartsyatem.repository.TeachingAssignmentRepository;
import com.smartsyatem.repository.TimetableEntryRepository;
import com.smartsyatem.repository.UserAccountRepository;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*") 
public class LoginApiController {

    private final UserAccountRepository repository;
    private final BatchRepository batchRepository;
    private final CourseRepository courseRepository;
    private final SemesterSettingsRepository settingsRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final AttendanceRepository attendanceRepository;

    public LoginApiController(UserAccountRepository repository, BatchRepository batchRepository, CourseRepository courseRepository, SemesterSettingsRepository settingsRepository, TeachingAssignmentRepository teachingAssignmentRepository, TimetableEntryRepository timetableEntryRepository, AttendanceRepository attendanceRepository) {
        this.repository = repository;
        this.batchRepository = batchRepository;
        this.courseRepository = courseRepository;
        this.settingsRepository = settingsRepository;
        this.teachingAssignmentRepository = teachingAssignmentRepository;
        this.timetableEntryRepository = timetableEntryRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody LoginForm loginForm) {
        String submittedUsername = loginForm.getUsername() != null ? loginForm.getUsername().trim() : "";
        String submittedRole = loginForm.getRole() != null ? loginForm.getRole().trim().toLowerCase() : "";
        String submittedPassword = loginForm.getPassword() != null ? loginForm.getPassword().trim() : "";

        System.out.println("Login attempt: Username='" + submittedUsername + "', Role='" + submittedRole + "'");
        
        Optional<UserAccount> user = repository.findByRoleIgnoreCaseAndUsernameIgnoreCase(submittedRole, submittedUsername);
        if (user.isEmpty()) {
            user = repository.findByUsernameIgnoreCase(submittedUsername);
        }

        if (user.isPresent() && user.get().getPassword().trim().equals(submittedPassword)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", user.get());
            System.out.println("Login successful for user: " + submittedUsername);
            return ResponseEntity.ok(response);
        }

        System.out.println("Login failed for user: " + submittedUsername);
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", "Invalid username or password.");
        return ResponseEntity.status(401).body(error);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> userData) {
        String username = userData.get("username");
        String role = userData.get("role") != null ? userData.get("role").toLowerCase() : "student";

        // Check if username is already taken by ANY role to avoid DB constraint violations
        boolean exists = repository.findByRoleAndUsername("student", username).isPresent() ||
                         repository.findByRoleAndUsername("staff", username).isPresent() ||
                         repository.findByRoleAndUsername("admin", username).isPresent();

        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "User already exists"));
        }

        UserAccount newUser = new UserAccount(username, userData.get("password"), role, userData.get("name"));
        if ("staff".equalsIgnoreCase(role)) {
            newUser.setEmail(username);
            // Handle initial teaching assignment from registration form
            if (userData.get("staffSubject") != null && !userData.get("staffSubject").isEmpty()) {
                TeachingAssignment ta = new TeachingAssignment();
                ta.setSubject(userData.get("staffSubject"));
                ta.setBatchId(userData.get("staffClassId"));
                if (userData.get("staffSemester") != null && !userData.get("staffSemester").isBlank()) {
                    try {
                        ta.setSemester(Integer.valueOf(userData.get("staffSemester")));
                    } catch (NumberFormatException e) {
                        ta.setSemester(1);
                    }
                } else {
                    ta.setSemester(1);
                }
                ta.setStaff(newUser);
                newUser.getTeachingAssignments().add(ta);
            }
        }
        if (userData.containsKey("batchId")) newUser.setBatchId(userData.get("batchId"));
        newUser.setIsApproved(true); // Auto-approve all registrations
        repository.save(newUser);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    // --- Placeholder API Endpoints for Dashboards ---

    // Admin Dashboard Endpoints
    @GetMapping("/admin/dashboard-stats")
    public ResponseEntity<?> getAdminDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        long studentsCount = repository.countByRole("student");
        long staffCount = repository.countByRole("staff");
        
        stats.put("totalStudents", studentsCount);
        stats.put("totalStaff", staffCount);

        // Calculate year-wise distribution
        SemesterSettings settings = settingsRepository.findAll().stream().findFirst().orElse(null);
        List<UserAccount> students = repository.findByRole("student");
        
        int y1 = 0, y2 = 0, y3 = 0, y4 = 0;
        if (settings != null) {
            for (UserAccount s : students) {
                Integer sem = calculateSemesterForBatch(s.getBatchId(), settings);
                if (sem != null) {
                    if (sem <= 2) y1++;
                    else if (sem <= 4) y2++;
                    else if (sem <= 6) y3++;
                    else if (sem <= 8) y4++;
                }
            }
        }
        stats.put("year1", y1);
        stats.put("year2", y2);
        stats.put("year3", y3);
        stats.put("finalYear", y4);
        stats.put("permStaff", staffCount);
        stats.put("tempStaff", 0); // Placeholder
        stats.put("academicYear", settings != null ? settings.getAcademicYear() : "2025-2026");
        stats.put("semesterType", settings != null ? settings.getSemesterType() : "odd");
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/admin/users")
    public List<UserAccount> getAllUsers() {
        return repository.findAll();
    }

    @PostMapping("/admin/users")
    public ResponseEntity<?> addUser(@RequestBody UserAccount user) {
        // Normalize role to lowercase to match login logic
        if (user.getRole() != null) {
            user.setRole(user.getRole().toLowerCase());
        }

        if (user.getUsername() == null && user.getIdentifier() != null) {
            user.setUsername(user.getIdentifier());
        }

        boolean exists = repository.findByRoleAndUsername("student", user.getUsername()).isPresent() ||
                         repository.findByRoleAndUsername("staff", user.getUsername()).isPresent() ||
                         repository.findByRoleAndUsername("admin", user.getUsername()).isPresent();
        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "User already exists"));
        }
        
        // Ensure that users added manually by admin are approved by default
        if (user.getIsApproved() == null) {
            user.setIsApproved(true);
        }

        // Save user first to get an ID for relationships
        UserAccount savedUser = repository.save(user);

        if ("STAFF".equalsIgnoreCase(savedUser.getRole()) && user.getTeachingAssignments() != null) {
            user.getTeachingAssignments().forEach(assignment -> assignment.setStaff(savedUser));
            teachingAssignmentRepository.saveAll(user.getTeachingAssignments());
        }
        
        if ("STUDENT".equalsIgnoreCase(savedUser.getRole()) && user.getEnrolledCourses() != null) {
            List<Course> coursesToEnroll = user.getEnrolledCourses().stream()
                .map(course -> courseRepository.findByName(course.getName()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
            savedUser.setEnrolledCourses(coursesToEnroll); // Set the actual Course entities
        }
        // Save again to persist relationships
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(savedUser));
    }

    @PutMapping("/admin/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserAccount userDetails) {
        return repository.findById(id)
                .map(user -> {
                    user.setName(userDetails.getName());
                    user.setUsername(userDetails.getIdentifier() != null ? userDetails.getIdentifier() : userDetails.getUsername());
                    user.setIdentifier(user.getUsername());
                    user.setPassword(userDetails.getPassword());
                    user.setRole(userDetails.getRole() != null ? userDetails.getRole().toLowerCase() : user.getRole());
                    user.setEmail(userDetails.getEmail());
                    user.setPhone(userDetails.getPhone());
                    user.setIsApproved(userDetails.getIsApproved());
                    user.setBatchId(userDetails.getBatchId());

                    if ("STAFF".equalsIgnoreCase(user.getRole())) {
                        // Handle teaching assignments
                        if (userDetails.getTeachingAssignments() != null) {
                            // Clear existing assignments to handle removals (assuming orphanRemoval=true on UserAccount's @OneToMany)
                            user.getTeachingAssignments().clear();
                            // Add new assignments from the request body, ensuring each is linked to the current staff user
                            for (TeachingAssignment assignment : userDetails.getTeachingAssignments()) {
                                assignment.setStaff(user); // Crucial: Link assignment to the staff member
                                user.getTeachingAssignments().add(assignment);
                            }
                        }
                    } else if ("STUDENT".equalsIgnoreCase(user.getRole())) {
                        // Handle enrolled courses
                        if (userDetails.getEnrolledCourses() != null) {
                            List<Course> coursesToEnroll = userDetails.getEnrolledCourses().stream()
                                .map(course -> {
                                    if (course.getId() != null) return courseRepository.findById(course.getId());
                                    return courseRepository.findByName(course.getName());
                                })
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .toList();
                            user.setEnrolledCourses(coursesToEnroll);
                        }
                    }
                    return ResponseEntity.ok(repository.save(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/admin/users/students/clear")
    @Transactional
    public ResponseEntity<?> clearAllStudents() {
        repository.deleteByRole("student");
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/batches")
    public List<Batch> getAdminBatches() {
        List<Batch> batches = batchRepository.findAll();
        SemesterSettings settings = settingsRepository.findAll().stream().findFirst().orElse(null);
        
        for (Batch batch : batches) {
            Integer sem = batch.getSemester();
            // If semester isn't manually set, calculate it based on global settings
            if (sem == null && settings != null) {
                sem = calculateSemesterForBatch(batch.getId(), settings);
                batch.setSemester(sem);
            }
            
            if (sem != null) {
                List<Course> courses = courseRepository.findBySemester(sem.longValue());
                batch.setCourseCount(courses.size());
            } else {
                batch.setCourseCount(0);
            }
        }
        return batches;
    }

    private Integer calculateSemesterForBatch(String batchId, SemesterSettings settings) {
        if (batchId == null || settings == null || settings.getAcademicYear() == null) return null;
        try {
            int startYear = Integer.parseInt(batchId.contains("-") ? batchId.split("-")[0] : batchId.substring(0, 4));
            int academicStartYear = Integer.parseInt(settings.getAcademicYear().split("-")[0]);
            int yearDiff = academicStartYear - startYear;
            int sem = (yearDiff * 2) + ("odd".equalsIgnoreCase(settings.getSemesterType()) ? 1 : 2);
            return Math.max(1, Math.min(8, sem));
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            // Invalid batchId or academicYear format
            return null;
        }
    }

    @PostMapping("/admin/batches")
    public ResponseEntity<?> addBatch(@RequestBody Batch batch) {
        if (batchRepository.existsById(batch.getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Batch ID already exists"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(batchRepository.save(batch));
    }

    @PutMapping("/admin/batches/{id}")
    public ResponseEntity<?> updateBatch(@PathVariable String id, @RequestBody Batch batch) {
        return batchRepository.findById(id)
                .map(existingBatch -> {
                    existingBatch.setName(batch.getName());
                    existingBatch.setSemester(batch.getSemester());
                    // courseCount is now dynamically calculated, no direct update here
                    return ResponseEntity.ok(batchRepository.save(existingBatch));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @SuppressWarnings("null")
	@DeleteMapping("/admin/batches/{id}")
    public ResponseEntity<?> deleteBatch(@PathVariable String id) {
        if (batchRepository.existsById(id)) {
            batchRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/admin/semester-settings")
    public ResponseEntity<?> getSemesterSettings() {
        return settingsRepository.findAll().stream().findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/admin/semester-settings")
    public ResponseEntity<?> saveSemesterSettings(@RequestBody @NonNull SemesterSettings settings) {
        return ResponseEntity.ok(settingsRepository.save(settings));
    }

    @PutMapping("/admin/semester-settings/{id}")
    public ResponseEntity<?> updateSemesterSettings(@PathVariable @NonNull Long id, @RequestBody SemesterSettings settings) {
        return settingsRepository.findById(id)
                .map(existing -> {
                    existing.setAcademicYear(settings.getAcademicYear());
                    existing.setSemesterType(settings.getSemesterType());
                    existing.setStartDate(settings.getStartDate()); 
                    existing.setEndDate(settings.getEndDate());
                    return ResponseEntity.ok(settingsRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/admin/courses")
    public List<Course> getCoursesBySemester(@RequestParam(required = false) Long semester) {
        if (semester != null) {
            return courseRepository.findBySemester(semester);
        }
        return courseRepository.findAll();
    }

    @PostMapping("/admin/courses")
    public ResponseEntity<?> addCourse(@RequestBody Course course) {
        Course savedCourse = courseRepository.save(course);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCourse);
    }

    @PutMapping("/admin/courses/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable Long id, @RequestBody Course course) {
        return courseRepository.findById(id)
                .map(existingCourse -> {
                    existingCourse.setCode(course.getCode());
                    existingCourse.setName(course.getName());
                    existingCourse.setShortName(course.getShortName());
                    existingCourse.setType(course.getType());
                    existingCourse.setCredits(course.getCredits());
                    existingCourse.setSemester(course.getSemester());
                    return ResponseEntity.ok(courseRepository.save(existingCourse));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/courses/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        if (courseRepository.existsById(id)) {
            courseRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Student Dashboard Endpoints
    @GetMapping("/student/profile/{regNo}")
    public ResponseEntity<?> getStudentProfile(@PathVariable String regNo) {
        return repository.findByRoleAndUsername("student", regNo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/student/profile/{regNo}")
    public ResponseEntity<?> updateStudentProfile(@PathVariable String regNo, @RequestBody UserAccount updatedUser) {
        return repository.findByRoleAndUsername("student", regNo)
                .map(user -> {
                    user.setName(updatedUser.getName());
                    if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                        user.setPassword(updatedUser.getPassword());
                    }
                    user.setEmail(updatedUser.getEmail());
                    user.setPhone(updatedUser.getPhone());
                    
                    // Handle enrolled courses update
                    if (updatedUser.getEnrolledCourses() != null) {
                        List<Course> managedCourses = updatedUser.getEnrolledCourses().stream()
                            .map(c -> courseRepository.findById(c.getId()))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .toList();
                        user.setEnrolledCourses(managedCourses);
                    }
                    
                    return ResponseEntity.ok(repository.save(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/attendance/{regNo}")
    public ResponseEntity<?> getStudentAttendance(@PathVariable String regNo) {
        return repository.findByRoleAndUsername("student", regNo)
                .map(student -> ResponseEntity.ok(attendanceRepository.findByStudent(student)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/timetable/admin")
    public List<TimetableEntry> getAdminTimetable(@RequestParam String year) {
        return timetableEntryRepository.findByYear(year);
    }

    @PostMapping("/timetable/admin")
    @Transactional
    public ResponseEntity<?> saveAdminTimetable(@RequestParam String year, @RequestBody List<TimetableEntry> entries) {
        timetableEntryRepository.deleteByYear(year);
        entries.forEach(e -> e.setYear(year));
        return ResponseEntity.ok(timetableEntryRepository.saveAll(entries));
    }

    @DeleteMapping("/timetable/admin")
    @Transactional
    public ResponseEntity<?> deleteAdminTimetable(@RequestParam String year) {
        timetableEntryRepository.deleteByYear(year);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/attendance/enrolled-students")
    public List<UserAccount> getEnrolledStudents(@RequestParam Long courseId) {
        return repository.findAll().stream()
                .filter(u -> "student".equalsIgnoreCase(u.getRole()))
                .filter(u -> u.getEnrolledCourses().stream().anyMatch(c -> c.getId().equals(courseId)))
                .toList();
    }

    @PostMapping("/staff/attendance")
    public ResponseEntity<?> markAttendance(@RequestBody List<Map<String, Object>> attendanceRecords) {
        for (Map<String, Object> record : attendanceRecords) {
            Long studentId = Long.valueOf(record.get("studentId").toString());
            String subject = record.get("subject").toString();
            String status = record.get("status").toString();
            Integer period = Integer.valueOf(record.get("period").toString());
            LocalDate date = LocalDate.parse(record.get("date").toString());

            UserAccount student = repository.findById(studentId).orElse(null);
            Course course = courseRepository.findByName(subject).orElse(null);

            if (student != null && course != null) {
                Attendance attendance = attendanceRepository.findByStudentAndDateAndPeriod(student, date, period)
                        .orElse(new Attendance());
                attendance.setStudent(student);
                attendance.setCourse(course);
                attendance.setStatus(status);
                attendance.setPeriod(period);
                attendance.setDate(date);
                attendanceRepository.save(attendance);
            }
        }
        return ResponseEntity.ok(Map.of("message", "Attendance marked successfully"));
    }

    // Staff Dashboard Endpoints
    @GetMapping("/staff/dashboard-stats/{username}")
    public ResponseEntity<?> getStaffDashboardStats(@PathVariable String username) {
        return repository.findByRoleAndUsername("staff", username)
                .map(staff -> {
                    Map<String, Object> stats = new HashMap<>();
                    int classes = staff.getTeachingAssignments().size();
                    int students = (int) repository.findAll().stream()
                            .filter(u -> "student".equalsIgnoreCase(u.getRole()))
                            .filter(u -> u.getEnrolledCourses().stream()
                                    .anyMatch(c -> staff.getTeachingAssignments().stream()
                                            .anyMatch(ta -> ta.getSubject().equalsIgnoreCase(c.getName()))))
                            .count();
                    stats.put("classesHandled", classes);
                    stats.put("totalStudentsAssigned", students);
                    stats.put("weeklyHours", classes * 3); // Mock calculation
                    return ResponseEntity.ok(stats);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/staff/profile/{email}")
    public ResponseEntity<?> getStaffProfile(@PathVariable String email) {
        return repository.findByRoleAndUsername("staff", email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/staff/profile/{email}")
    public ResponseEntity<?> updateStaffProfile(@PathVariable String email, @RequestBody UserAccount updatedUser) {
        return repository.findByRoleAndUsername("staff", email)
                .map(user -> {
                    if (updatedUser.getName() != null) user.setName(updatedUser.getName());
                    if (updatedUser.getEmail() != null) user.setEmail(updatedUser.getEmail());
                    if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                        user.setPassword(updatedUser.getPassword());
                    }
                    // If the request comes from staff_settings.html with simple fields
                    return ResponseEntity.ok(repository.save(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/staff/students-for-attendance/{batchId}/{subject}")
    public ResponseEntity<?> getStudentsForAttendance(@PathVariable String batchId, @PathVariable String subject) {
        String cleanSubject = subject != null ? subject.trim() : "";
        String cleanBatchId = batchId != null ? batchId.trim() : "";

        // Find course matching subject by name, code, or shortName
        Optional<Course> courseOpt = courseRepository.findAll().stream()
                .filter(c -> (c.getName() != null && c.getName().equalsIgnoreCase(cleanSubject)) ||
                             (c.getCode() != null && c.getCode().equalsIgnoreCase(cleanSubject)) ||
                             (c.getShortName() != null && c.getShortName().equalsIgnoreCase(cleanSubject)))
                .findFirst();

        Long targetCourseId = courseOpt.map(Course::getId).orElse(null);
        String targetCourseName = courseOpt.map(Course::getName).orElse(cleanSubject);

        List<UserAccount> students = repository.findAll().stream()
                .filter(u -> "student".equalsIgnoreCase(u.getRole()))
                .filter(u -> {
                    // Check enrollment if enrolledCourses is populated
                    if (u.getEnrolledCourses() != null && !u.getEnrolledCourses().isEmpty()) {
                        return u.getEnrolledCourses().stream().anyMatch(c ->
                            (targetCourseId != null && targetCourseId.equals(c.getId())) ||
                            (c.getName() != null && c.getName().equalsIgnoreCase(targetCourseName)) ||
                            (c.getCode() != null && c.getCode().equalsIgnoreCase(cleanSubject)) ||
                            (c.getShortName() != null && c.getShortName().equalsIgnoreCase(cleanSubject))
                        );
                    }
                    // Fallback: If no student has explicit course enrollment yet, match by batch or include student
                    return true;
                })
                .map(u -> {
                    UserAccount summary = new UserAccount();
                    summary.setId(u.getId());
                    summary.setName(u.getName());
                    summary.setIdentifier(u.getIdentifier() != null ? u.getIdentifier() : u.getUsername());
                    summary.setUsername(u.getUsername());
                    summary.setEmail(u.getEmail());
                    summary.setPhone(u.getPhone());
                    summary.setDob(u.getDob());
                    return summary;
                })
                .toList();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/admin/attendance-summary")
    public ResponseEntity<?> getAttendanceSummary() {
        List<Attendance> allAttendance = attendanceRepository.findAll();
        List<UserAccount> students = repository.findByRole("student");
        
        Map<String, Map<String, Object>> summary = new HashMap<>();

        for (UserAccount student : students) {
            String identifier = student.getIdentifier() != null ? student.getIdentifier() : student.getUsername();
            List<Attendance> studentRecords = allAttendance.stream()
                    .filter(a -> a.getStudent() != null && a.getStudent().getId().equals(student.getId()))
                    .toList();

            long total = studentRecords.size();
            long present = studentRecords.stream()
                    .filter(a -> "present".equalsIgnoreCase(a.getStatus()) || "late".equalsIgnoreCase(a.getStatus()))
                    .count();

            double pct = total > 0 ? Math.round(((double) present / total) * 1000.0) / 10.0 : 85.0; // Default clean 85% for demo if no attendance recorded yet

            Map<String, Object> data = new HashMap<>();
            data.put("total", total);
            data.put("present", present);
            data.put("percentage", pct);

            if (identifier != null) summary.put(identifier, data);
            if (student.getUsername() != null) summary.put(student.getUsername(), data);
            if (student.getId() != null) summary.put(student.getId().toString(), data);
        }
        return ResponseEntity.ok(summary);
    }
}