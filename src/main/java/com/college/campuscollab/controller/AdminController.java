package com.college.campuscollab.controller;

import com.college.campuscollab.dto.*;
import com.college.campuscollab.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
// Authentication handled by SecurityConfig - JWT must be valid
public class AdminController {

    private final AdminService adminService;

    /**
     * Get Admin Dashboard Statistics
     * Returns: totalStudents, totalProjects, pendingRequests, activeCollaborations
     */
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getAdminStats() {
        AdminStatsDTO stats = adminService.getAdminStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get All Students
     * Optional filter by course
     */
    @GetMapping("/students")
    public ResponseEntity<List<StudentDTO>> getAllStudents(
            @RequestParam(required = false) String course) {
        List<StudentDTO> students = adminService.getAllStudents(course);
        return ResponseEntity.ok(students);
    }

    /**
     * Update Student Details (Admin can edit any student)
     */
    @PutMapping("/students/{studentId}")
    public ResponseEntity<StudentDTO> updateStudent(
            @PathVariable Long studentId,
            @RequestBody UpdateProfileRequest updateRequest) {
        StudentDTO updated = adminService.updateStudent(studentId, updateRequest);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete Student (Admin can delete any student)
     */
    @DeleteMapping("/students/{studentId}")
    public ResponseEntity<String> deleteStudent(@PathVariable Long studentId) {
        adminService.deleteStudent(studentId);
        return ResponseEntity.ok("Student deleted successfully");
    }

    /**
     * Change Student Password (Admin can change any student's password)
     */
    @PutMapping("/students/{studentId}/change-password")
    public ResponseEntity<String> changeStudentPassword(
            @PathVariable Long studentId,
            @RequestBody java.util.Map<String, String> request) {
        String newPassword = request.get("newPassword");
        adminService.changeStudentPassword(studentId, newPassword);
        return ResponseEntity.ok("Password changed successfully");
    }

    /**
     * Get All Projects (Admin view - includes all statuses)
     */
    @GetMapping("/projects")
    public ResponseEntity<List<AdminProjectDTO>> getAllProjects() {
        List<AdminProjectDTO> projects = adminService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    /**
     * Approve Project (Admin can approve any project)
     */
    @PutMapping("/projects/{projectId}/approve")
    public ResponseEntity<String> approveProject(@PathVariable Long projectId) {
        adminService.approveProject(projectId);
        return ResponseEntity.ok("Project approved successfully");
    }

    /**
     * Reject Project (Admin can reject any project)
     */
    @PutMapping("/projects/{projectId}/reject")
    public ResponseEntity<String> rejectProject(@PathVariable Long projectId) {
        adminService.rejectProject(projectId);
        return ResponseEntity.ok("Project rejected successfully");
    }

    /**
     * Delete Project (Admin can delete any project)
     */
    @DeleteMapping("/projects/{projectId}")
    public ResponseEntity<String> deleteProject(@PathVariable Long projectId) {
        adminService.deleteProject(projectId);
        return ResponseEntity.ok("Project deleted successfully");
    }

    /**
     * Get All Contribution Requests (All requests in system)
     */
    @GetMapping("/requests")
    public ResponseEntity<List<AdminRequestDTO>> getAllContributionRequests() {
        List<AdminRequestDTO> requests = adminService.getAllContributionRequests();
        return ResponseEntity.ok(requests);
    }

    /**
     * Approve Contribution Request (Admin can approve any request)
     */
    @PutMapping("/requests/{requestId}/approve")
    public ResponseEntity<String> approveRequest(@PathVariable Long requestId) {
        adminService.approveContributionRequest(requestId);
        return ResponseEntity.ok("Request approved successfully");
    }

    /**
     * Reject Contribution Request (Admin can reject any request)
     */
    @PutMapping("/requests/{requestId}/reject")
    public ResponseEntity<String> rejectRequest(@PathVariable Long requestId) {
        adminService.rejectContributionRequest(requestId);
        return ResponseEntity.ok("Request rejected successfully");
    }

    /**
     * Send Bulk Email to Selected Students with Optional Attachments
     */
    @PostMapping(value = "/send-bulk-email", consumes = "multipart/form-data")
    public ResponseEntity<?> sendBulkEmail(
            @RequestPart("emailData") String emailDataJson,
            @RequestPart(value = "attachments", required = false) org.springframework.web.multipart.MultipartFile[] attachments) {
        try {
            // Parse JSON string to BulkEmailRequest
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.college.campuscollab.dto.BulkEmailRequest emailRequest = objectMapper.readValue(emailDataJson,
                    com.college.campuscollab.dto.BulkEmailRequest.class);

            String result = adminService.sendBulkEmailToStudents(emailRequest, attachments);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to send bulk email: " + e.getMessage());
        }
    }

    /**
     * Cleanup Orphaned Requests (requests whose projects have been deleted)
     */
    @DeleteMapping("/cleanup-orphaned-requests")
    public ResponseEntity<String> cleanupOrphanedRequests() {
        int deletedCount = adminService.cleanupOrphanedRequests();
        return ResponseEntity.ok(deletedCount + " orphaned requests deleted successfully");
    }

    /**
     * Get Student Leaderboard
     * GET /api/admin/leaderboard
     * Returns top performing students based on projects, collaborations, and likes
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<List<java.util.Map<String, Object>>> getLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        List<java.util.Map<String, Object>> leaderboard = adminService.getLeaderboard(limit);
        return ResponseEntity.ok(leaderboard);
    }
}
