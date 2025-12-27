package com.college.campuscollab.service;

import com.college.campuscollab.dto.*;
import com.college.campuscollab.entity.*;
import com.college.campuscollab.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ContributionRequestRepository contributionRequestRepository;
    private final ActivityLogService activityLogService;
    private final EmailService emailService;
    private final ActivityLogRepository activityLogRepository;
    private final ProjectLikeRepository projectLikeRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final ProblemSubmissionRepository problemSubmissionRepository;
    private final ProblemRecordRepository problemRecordRepository;

    private User getCurrentAdmin() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
    }

    // Get Dashboard Statistics
    public AdminStatsDTO getAdminStats() {
        long totalStudents = userRepository.countByRole(Role.STUDENT);
        long totalProjects = projectRepository.count();
        long pendingRequests = contributionRequestRepository.countByStatus(RequestStatus.PENDING);
        long activeCollaborations = contributionRequestRepository.countByStatus(RequestStatus.APPROVED);

        return new AdminStatsDTO(totalStudents, totalProjects, pendingRequests, activeCollaborations);
    }

    // Get All Students
    public List<StudentDTO> getAllStudents(String course) {
        List<User> students;

        if (course != null && !course.trim().isEmpty()) {
            students = userRepository.findByRoleAndCourse(Role.STUDENT, course);
        } else {
            students = userRepository.findByRole(Role.STUDENT);
        }

        return students.stream()
                .map(this::convertToStudentDTO)
                .collect(Collectors.toList());
    }

    // Convert User to StudentDTO
    private StudentDTO convertToStudentDTO(User user) {
        StudentDTO dto = new StudentDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setRollNumber(user.getRollNumber());
        dto.setCourse(user.getCourse());
        dto.setSemester(user.getSemester());
        dto.setRole(user.getRole().name());

        // Count projects by this user
        List<Project> userProjects = projectRepository.findByOwner(user);
        dto.setProjectCount(userProjects.size());

        // Account Status
        dto.setStatus(user.getAccountStatus() != null ? user.getAccountStatus().name().toLowerCase() : "active");

        // Last Active
        dto.setLastActive(user.getLastLoginAt() != null ? user.getLastLoginAt() : LocalDateTime.now());

        // Join Date
        dto.setJoinDate(user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now());

        return dto;
    }

    // Get All Projects (Admin View)
    public List<AdminProjectDTO> getAllProjects() {
        List<Project> projects = projectRepository.findAllByOrderByCreatedAtDesc();

        return projects.stream()
                .map(this::convertToAdminProjectDTO)
                .collect(Collectors.toList());
    }

    // Convert Project to AdminProjectDTO
    private AdminProjectDTO convertToAdminProjectDTO(Project project) {
        AdminProjectDTO dto = new AdminProjectDTO();
        dto.setId(project.getId());
        dto.setProjectName(project.getProjectName());
        dto.setTeamLeaderName(project.getTeamLeaderName());
        dto.setOwnerName(project.getOwner().getFullName());
        dto.setOwnerEmail(project.getOwner().getEmail());
        dto.setCourse(project.getCourse());
        dto.setSemester(project.getSemester());
        dto.setTechStack(project.getTechStack());
        dto.setDescription(project.getDescription());
        dto.setLiveLink(project.getLiveLink());
        dto.setCodeLink(project.getCodeLink());
        dto.setStatus(project.getStatus());
        dto.setViewCount(project.getViewCount());
        dto.setLikeCount(project.getLikeCount());
        dto.setScreenshots(project.getScreenshots());
        dto.setCreatedAt(project.getCreatedAt());

        return dto;
    }

    // Approve Project
    @Transactional
    public void approveProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with ID: " + projectId));

        String oldStatus = project.getStatus();
        project.setStatus("APPROVED");
        projectRepository.save(project);

        // Log activity
        User admin = getCurrentAdmin();
        activityLogService.logUpdateActivity(
                admin,
                "PROJECT_APPROVED",
                "ADMIN",
                "Admin " + admin.getFullName() + " approved project '" + project.getProjectName() + "'",
                "Project",
                project.getId(),
                oldStatus,
                "APPROVED");
    }

    // Reject Project
    @Transactional
    public void rejectProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with ID: " + projectId));

        String oldStatus = project.getStatus();
        project.setStatus("REJECTED");
        projectRepository.save(project);

        // Log activity
        User admin = getCurrentAdmin();
        activityLogService.logUpdateActivity(
                admin,
                "PROJECT_REJECTED",
                "ADMIN",
                "Admin " + admin.getFullName() + " rejected project '" + project.getProjectName() + "'",
                "Project",
                project.getId(),
                oldStatus,
                "REJECTED");
    }

    // Delete Project (Admin can delete any project)
    @Transactional
    public void deleteProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with ID: " + projectId));

        String projectName = project.getProjectName();

        System.out.println("\n🗑️ Deleting project: " + projectName + " (ID: " + projectId + ")");

        // Step 1: Delete all contribution requests for this project
        List<ContributionRequest> projectRequests = contributionRequestRepository.findByProject(project);
        if (!projectRequests.isEmpty()) {
            System.out
                    .println("🗑️ Deleting " + projectRequests.size() + " contribution request(s) for this project...");
            contributionRequestRepository.deleteAll(projectRequests);
        }

        // Step 2: Delete all likes for this project
        System.out.println("🗑️ Deleting project likes...");
        projectLikeRepository.deleteByProject(project);

        // Step 3: Finally delete the project
        System.out.println("🗑️ Deleting project from database...");
        projectRepository.deleteById(projectId);
        System.out.println("✅ Project deleted successfully");

        // Log activity
        User admin = getCurrentAdmin();
        activityLogService.logEntityActivity(
                admin,
                "PROJECT_DELETED",
                "ADMIN",
                "Admin " + admin.getFullName() + " deleted project '" + projectName + "'",
                "Project",
                projectId);

        System.out.println("✅ Project deletion complete\n");
    }

    // Update Student Details
    @Transactional
    public StudentDTO updateStudent(Long studentId, UpdateProfileRequest updateRequest) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        StringBuilder changes = new StringBuilder();

        // Admin can update ALL fields including email and status
        if (updateRequest.getFullName() != null && !updateRequest.getFullName().equals(student.getFullName())) {
            changes.append("Name: ").append(student.getFullName()).append(" → ").append(updateRequest.getFullName())
                    .append("; ");
            student.setFullName(updateRequest.getFullName());
        }
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(student.getEmail())) {
            // Check if email is already taken by another user
            if (userRepository.existsByEmail(updateRequest.getEmail())) {
                User existingUser = userRepository.findByEmail(updateRequest.getEmail()).orElse(null);
                if (existingUser != null && !existingUser.getId().equals(studentId)) {
                    throw new RuntimeException("Email already registered with another account");
                }
            }
            changes.append("Email: ").append(student.getEmail()).append(" → ").append(updateRequest.getEmail())
                    .append("; ");
            student.setEmail(updateRequest.getEmail());
        }
        if (updateRequest.getRollNumber() != null && !updateRequest.getRollNumber().equals(student.getRollNumber())) {
            changes.append("Roll: ").append(student.getRollNumber()).append(" → ").append(updateRequest.getRollNumber())
                    .append("; ");
            student.setRollNumber(updateRequest.getRollNumber());
        }
        if (updateRequest.getCourse() != null && !updateRequest.getCourse().equals(student.getCourse())) {
            changes.append("Course: ").append(student.getCourse()).append(" → ").append(updateRequest.getCourse())
                    .append("; ");
            student.setCourse(updateRequest.getCourse());
        }
        if (updateRequest.getSemester() != null && !updateRequest.getSemester().equals(student.getSemester())) {
            changes.append("Semester: ").append(student.getSemester()).append(" → ").append(updateRequest.getSemester())
                    .append("; ");
            student.setSemester(updateRequest.getSemester());
        }
        // Update Account Status
        if (updateRequest.getStatus() != null) {
            try {
                AccountStatus newStatus = AccountStatus.valueOf(updateRequest.getStatus().toUpperCase());
                if (!newStatus.equals(student.getAccountStatus())) {
                    changes.append("Status: ").append(student.getAccountStatus()).append(" → ").append(newStatus)
                            .append("; ");
                    student.setAccountStatus(newStatus);
                }
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status value. Use ACTIVE, SUSPENDED, or INACTIVE");
            }
        }

        User updatedStudent = userRepository.save(student);

        // Log activity if there were changes
        if (changes.length() > 0) {
            User admin = getCurrentAdmin();
            activityLogService.logActivity(
                    admin,
                    "STUDENT_UPDATED",
                    "ADMIN",
                    "Admin " + admin.getFullName() + " updated student " + student.getFullName() + ": "
                            + changes.toString());
        }

        return convertToStudentDTO(updatedStudent);
    }

    // Delete Student
    @Transactional
    public void deleteStudent(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        String studentName = student.getFullName();
        String studentEmail = student.getEmail();

        System.out.println("\n🗑️ Deleting student: " + studentName + " (ID: " + studentId + ")");

        // Step 1: Delete project likes by this student
        System.out.println("🗑️ Deleting project likes...");
        projectLikeRepository.deleteByUser(student);

        // Step 2: Delete password reset tokens
        System.out.println("🗑️ Deleting password reset tokens...");
        passwordResetTokenRepository.deleteByUser(student);

        // Step 3: Delete quiz submissions
        System.out.println("🗑️ Deleting quiz submissions...");
        quizSubmissionRepository.deleteByUser(student);

        // Step 4: Delete problem submissions
        System.out.println("🗑️ Deleting problem submissions...");
        problemSubmissionRepository.deleteByUser(student);

        // Step 5: Delete problem records
        System.out.println("🗑️ Deleting problem records...");
        problemRecordRepository.deleteByUser(student);

        // Step 6: Delete contribution requests made BY student
        List<ContributionRequest> requestsByStudent = contributionRequestRepository.findByRequestedBy(student);
        if (!requestsByStudent.isEmpty()) {
            System.out.println(
                    "🗑️ Deleting " + requestsByStudent.size() + " contribution request(s) made by student...");
            contributionRequestRepository.deleteAll(requestsByStudent);
        }

        // Step 7: Delete contribution requests made TO student's projects
        List<Project> studentProjects = projectRepository.findByOwner(student);
        for (Project project : studentProjects) {
            List<ContributionRequest> requestsToProject = contributionRequestRepository.findByProject(project);
            if (!requestsToProject.isEmpty()) {
                System.out.println("🗑️ Deleting " + requestsToProject.size()
                        + " contribution request(s) for project: " + project.getProjectName());
                contributionRequestRepository.deleteAll(requestsToProject);
            }
        }

        // Step 8: Delete student's projects
        if (!studentProjects.isEmpty()) {
            System.out.println("🗑️ Deleting " + studentProjects.size() + " project(s) owned by student...");
            projectRepository.deleteAll(studentProjects);
        }

        // Step 9: Delete activity logs for this user
        System.out.println("🗑️ Deleting activity logs for student...");
        activityLogRepository.deleteByUser(student);

        // Step 10: Finally delete the student
        System.out.println("🗑️ Deleting student from database...");
        userRepository.deleteById(studentId);
        System.out.println("✅ Student deleted successfully");

        // Log activity (after deleting student logs, log this final action)
        User admin = getCurrentAdmin();
        activityLogService.logEntityActivity(
                admin,
                "STUDENT_DELETED",
                "ADMIN",
                "Admin " + admin.getFullName() + " deleted student " + studentName + " (" + studentEmail
                        + ") and all related data",
                "User",
                studentId);

        System.out.println("✅ Student deletion complete\n");
    }

    // Change Student Password (Admin can change any student's password)
    @Transactional
    public void changeStudentPassword(Long studentId, String newPassword) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        // Encode the new password
        org.springframework.security.crypto.password.PasswordEncoder passwordEncoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(newPassword);

        student.setPassword(encodedPassword);
        userRepository.save(student);

        // Log activity
        User admin = getCurrentAdmin();
        activityLogService.logActivity(
                admin,
                "STUDENT_PASSWORD_CHANGED",
                "ADMIN",
                "Admin " + admin.getFullName() + " changed password for student " + student.getFullName() +
                        " (" + student.getEmail() + ")");

        System.out.println("✅ Password changed successfully for student: " + student.getFullName());
    }

    // Get All Contribution Requests
    public List<AdminRequestDTO> getAllContributionRequests() {
        List<ContributionRequest> requests = contributionRequestRepository.findAll();

        // Filter out orphaned requests (where project has been deleted)
        return requests.stream()
                .filter(request -> request.getProject() != null) // Only include requests with active projects
                .map(this::convertToAdminRequestDTO)
                .collect(Collectors.toList());
    }

    // Convert ContributionRequest to AdminRequestDTO
    private AdminRequestDTO convertToAdminRequestDTO(ContributionRequest request) {
        AdminRequestDTO dto = new AdminRequestDTO();
        dto.setId(request.getId());
        dto.setProjectId(request.getProject().getId());
        dto.setProjectName(request.getProject().getProjectName());
        dto.setOwnerName(request.getProject().getOwner().getFullName());
        dto.setOwnerEmail(request.getProject().getOwner().getEmail());
        dto.setRequesterName(request.getRequestedBy().getFullName());
        dto.setRequesterEmail(request.getRequestedBy().getEmail());
        dto.setMessage(request.getMessage());
        dto.setStatus(request.getStatus().name());
        dto.setRequestedAt(request.getRequestedAt());

        return dto;
    }

    // Admin Approve Request (Admin can approve any request)
    @Transactional
    public void approveContributionRequest(Long requestId) {
        ContributionRequest request = contributionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found with ID: " + requestId));

        request.setStatus(RequestStatus.APPROVED);
        contributionRequestRepository.save(request);

        // Log activity
        User admin = getCurrentAdmin();
        activityLogService.logEntityActivity(
                admin,
                "REQUEST_APPROVED",
                "ADMIN",
                "Admin " + admin.getFullName() + " approved collaboration request for project '" +
                        request.getProject().getProjectName() + "' from " + request.getRequestedBy().getFullName(),
                "ContributionRequest",
                requestId);
    }

    // Admin Reject Request (Admin can reject any request)
    @Transactional
    public void rejectContributionRequest(Long requestId) {
        ContributionRequest request = contributionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found with ID: " + requestId));

        request.setStatus(RequestStatus.REJECTED);
        contributionRequestRepository.save(request);

        // Log activity
        User admin = getCurrentAdmin();
        activityLogService.logEntityActivity(
                admin,
                "REQUEST_REJECTED",
                "ADMIN",
                "Admin " + admin.getFullName() + " rejected collaboration request for project '" +
                        request.getProject().getProjectName() + "' from " + request.getRequestedBy().getFullName(),
                "ContributionRequest",
                requestId);
    }

    // Cleanup Orphaned Requests (requests whose projects have been deleted)
    @Transactional
    public int cleanupOrphanedRequests() {
        List<ContributionRequest> allRequests = contributionRequestRepository.findAll();

        // Find orphaned requests (project is null)
        List<ContributionRequest> orphanedRequests = allRequests.stream()
                .filter(request -> request.getProject() == null)
                .collect(Collectors.toList());

        int count = orphanedRequests.size();

        if (count > 0) {
            System.out.println("🧹 Cleaning up " + count + " orphaned requests...");
            contributionRequestRepository.deleteAll(orphanedRequests);
            System.out.println("✅ Cleanup complete!");
        } else {
            System.out.println("✅ No orphaned requests found");
        }

        return count;
    }

    // Send Bulk Email to Selected Students with Optional Attachments
    @Transactional
    public String sendBulkEmailToStudents(BulkEmailRequest emailRequest,
            org.springframework.web.multipart.MultipartFile[] attachments) {
        System.out.println("\n🚀 AdminService.sendBulkEmailToStudents() - START");
        System.out.println("📦 Request received: " + emailRequest);
        System.out.println("📎 Attachments received: " + (attachments != null ? attachments.length : 0));

        // Validate input
        boolean hasStudentIds = emailRequest.getStudentIds() != null && !emailRequest.getStudentIds().isEmpty();
        boolean hasAdditionalEmails = emailRequest.getAdditionalEmails() != null
                && !emailRequest.getAdditionalEmails().isEmpty();
        boolean hasAdditionalRecipients = emailRequest.getAdditionalRecipients() != null
                && !emailRequest.getAdditionalRecipients().isEmpty();

        System.out.println("🔍 hasStudentIds: " + hasStudentIds);
        System.out.println("🔍 hasAdditionalEmails: " + hasAdditionalEmails);
        System.out.println("🔍 hasAdditionalRecipients: " + hasAdditionalRecipients);

        if (!hasStudentIds && !hasAdditionalEmails && !hasAdditionalRecipients) {
            System.err.println("❌ Validation Failed: No recipients selected");
            System.err.println("   studentIds: " + emailRequest.getStudentIds());
            System.err.println("   additionalEmails: " + emailRequest.getAdditionalEmails());
            System.err.println("   additionalRecipients: " + emailRequest.getAdditionalRecipients());
            throw new RuntimeException("No recipients selected");
        }
        if (emailRequest.getSubject() == null || emailRequest.getSubject().trim().isEmpty()) {
            System.err.println("❌ Validation Failed: Email subject is required");
            throw new RuntimeException("Email subject is required");
        }
        if (emailRequest.getMessage() == null || emailRequest.getMessage().trim().isEmpty()) {
            System.err.println("❌ Validation Failed: Email message is required");
            throw new RuntimeException("Email message is required");
        }

        System.out.println("✅ Validation Passed");
        if (hasStudentIds) {
            System.out.println("📋 Student IDs: " + emailRequest.getStudentIds());
        }
        if (hasAdditionalEmails) {
            System.out.println("📋 Additional Emails: " + emailRequest.getAdditionalEmails());
        }
        System.out.println("📧 Subject: " + emailRequest.getSubject());
        System.out.println("📝 Message: " + emailRequest.getMessage());

        int totalRecipients = 0;
        int successCount = 0;
        int failCount = 0;

        // Send to registered students
        if (hasStudentIds) {
            System.out.println("\n🔍 Fetching registered students from database...");
            List<User> selectedStudents = userRepository.findAllById(emailRequest.getStudentIds());

            if (!selectedStudents.isEmpty()) {
                System.out.println("✅ Found " + selectedStudents.size() + " registered student(s):");
                for (User student : selectedStudents) {
                    System.out.println("   - " + student.getFullName() + " (" + student.getEmail() + ")");
                }

                System.out.println("\n📮 Sending emails to registered students...");
                for (User student : selectedStudents) {
                    try {
                        System.out.println("📤 Sending to: " + student.getEmail());
                        emailService.sendHtmlEmailWithAttachments(student.getEmail(), student.getFullName(),
                                emailRequest.getSubject(), emailRequest.getMessage(), attachments);
                        successCount++;
                        System.out.println("✅ Email sent to: " + student.getFullName());
                        Thread.sleep(200); // Small delay
                    } catch (Exception e) {
                        failCount++;
                        System.err.println("❌ Failed to send email to: " + student.getEmail());
                        System.err.println("Error: " + e.getMessage());
                    }
                    totalRecipients++;
                }
            }
        }

        // Send to additional (manual) email addresses (old format - backward
        // compatibility)
        if (hasAdditionalEmails)

        {
            System.out.println("\n📮 Sending emails to manual email addresses (old format)...");
            for (String email : emailRequest.getAdditionalEmails()) {
                try {
                    System.out.println("📤 Sending to: " + email);
                    emailService.sendHtmlEmailWithAttachments(email, "Recipient", emailRequest.getSubject(),
                            emailRequest.getMessage(), attachments);
                    successCount++;
                    System.out.println("✅ Email sent to: " + email);
                    Thread.sleep(200); // Small delay
                } catch (Exception e) {
                    failCount++;
                    System.err.println("❌ Failed to send email to: " + email);
                    System.err.println("Error: " + e.getMessage());
                }
                totalRecipients++;
            }
        }

        // Send to additional recipients with names (new format)
        if (hasAdditionalRecipients) {
            System.out.println("\n📮 Sending emails to manual recipients with names...");
            for (BulkEmailRequest.Recipient recipient : emailRequest.getAdditionalRecipients()) {
                try {
                    String recipientName = recipient.getName() != null && !recipient.getName().trim().isEmpty()
                            ? recipient.getName()
                            : "Recipient";

                    System.out.println("📤 Sending to: " + recipientName + " <" + recipient.getEmail() + ">");
                    emailService.sendHtmlEmailWithAttachments(recipient.getEmail(), recipientName,
                            emailRequest.getSubject(), emailRequest.getMessage(), attachments);
                    successCount++;
                    System.out.println("✅ Email sent to: " + recipientName);
                    Thread.sleep(200); // Small delay
                } catch (Exception e) {
                    failCount++;
                    System.err.println("❌ Failed to send email to: " + recipient.getEmail());
                    System.err.println("Error: " + e.getMessage());
                }
                totalRecipients++;
            }
        }

        // Log activity
        System.out.println("\n📊 Logging activity...");
        User admin = getCurrentAdmin();
        activityLogService.logActivity(admin, "BULK_EMAIL_SENT", "ADMIN", "Admin " + admin.getFullName()
                + " sent bulk email to " + totalRecipients + " recipient(s): \"" + emailRequest.getSubject() + "\"");

        System.out.println("\n====================================");
        System.out.println("📊 BULK EMAIL SUMMARY");
        System.out.println("====================================");
        System.out.println("✅ Success: " + successCount);
        System.out.println("❌ Failed: " + failCount);
        System.out.println("📈 Total: " + totalRecipients);
        System.out.println("====================================");
        System.out.println("✅ AdminService.sendBulkEmailToStudents() - COMPLETE\n");

        return "Email sent successfully to " + successCount + " out of " + totalRecipients + " recipient(s)";
    }

    /**
     * Get Student Le aderboard
     * Returns top performing students based on:
     * - Total Projects
     * - Total Collaborations (Approved Requests)
     * - Total Likes
     */
    public List<java.util.Map<String, Object>> getLeaderboard(int limit) {
        List<User> allStudents = userRepository.findByRole(Role.STUDENT);

        List<java.util.Map<String, Object>> leaderboard = allStudents.stream()
                .map(student -> {
                    // Count projects
                    int projectCount = projectRepository.findByOwner(student).size();

                    // Count approved collaborations (where student requested)
                    long collaborationCount = contributionRequestRepository
                            .countByRequestedByAndStatus(student, RequestStatus.APPROVED);

                    // Count total likes on student's projects
                    List<Project> studentProjects = projectRepository.findByOwner(student);
                    int totalLikes = studentProjects.stream()
                            .mapToInt(Project::getLikeCount)
                            .sum();

                    // Calculate total score (projects + collaborations + likes)
                    int totalScore = projectCount + (int) collaborationCount + totalLikes;

                    java.util.Map<String, Object> studentData = new java.util.HashMap<>();
                    studentData.put("id", student.getId());
                    studentData.put("name", student.getFullName());
                    studentData.put("email", student.getEmail());
                    studentData.put("projects", projectCount);
                    studentData.put("collaborations", collaborationCount);
                    studentData.put("likes", totalLikes);
                    studentData.put("totalScore", totalScore);

                    return studentData;
                })
                .filter(data -> (int) data.get("totalScore") > 0) // Only include students with activity
                .sorted((a, b) -> Integer.compare((int) b.get("totalScore"), (int) a.get("totalScore"))) // Sort by
                                                                                                         // score desc
                .limit(limit)
                .collect(Collectors.toList());

        // Add rank
        for (int i = 0; i < leaderboard.size(); i++) {
            leaderboard.get(i).put("rank", i + 1);
        }

        return leaderboard;
    }
}
