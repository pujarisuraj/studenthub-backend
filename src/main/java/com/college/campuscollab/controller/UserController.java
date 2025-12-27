package com.college.campuscollab.controller;

import com.college.campuscollab.dto.PendingRequestDTO;
import com.college.campuscollab.dto.UpdateProfileRequest;
import com.college.campuscollab.dto.UserProfileDTO;
import com.college.campuscollab.entity.ContributionRequest;
import com.college.campuscollab.entity.RequestStatus;
import com.college.campuscollab.entity.User;
import com.college.campuscollab.repository.ContributionRequestRepository;
import com.college.campuscollab.repository.ProjectRepository;
import com.college.campuscollab.service.UserService;
import com.college.campuscollab.service.ActivityLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:3001" }, allowCredentials = "true")
public class UserController {

    private final UserService userService;
    private final ProjectRepository projectRepository;
    private final ContributionRequestRepository contributionRequestRepository;
    private final ActivityLogService activityLogService;

    public UserController(UserService userService,
            ProjectRepository projectRepository,
            ContributionRequestRepository contributionRequestRepository,
            ActivityLogService activityLogService) {
        this.userService = userService;
        this.projectRepository = projectRepository;
        this.contributionRequestRepository = contributionRequestRepository;
        this.activityLogService = activityLogService;
    }

    /**
     * Get current user's profile
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile() {
        String email = getCurrentUserEmail();
        UserProfileDTO profile = userService.getUserProfile(email);
        return ResponseEntity.ok(profile);
    }

    /**
     * Update current user's profile
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@RequestBody UpdateProfileRequest request) {
        try {
            String email = getCurrentUserEmail();
            User user = userService.getUserByEmail(email);

            // Get old values for logging
            UserProfileDTO oldProfile = userService.getUserProfile(email);

            // Update profile
            userService.updateUserProfile(email, request);

            // Log profile update activity
            StringBuilder changes = new StringBuilder();
            if (request.getFullName() != null && !request.getFullName().equals(oldProfile.getFullName())) {
                changes.append("Name updated; ");
                activityLogService.logUpdateActivity(
                        user,
                        "PROFILE_UPDATE_NAME",
                        "USER",
                        "User " + user.getFullName() + " updated their name",
                        "User",
                        user.getId(),
                        oldProfile.getFullName(),
                        request.getFullName());
            }
            if (request.getRollNumber() != null && !request.getRollNumber().equals(oldProfile.getRollNumber())) {
                changes.append("Roll Number updated; ");
            }
            if (request.getCourse() != null && !request.getCourse().equals(oldProfile.getCourse())) {
                changes.append("Course updated; ");
            }
            if (request.getSemester() != null && !request.getSemester().equals(oldProfile.getSemester())) {
                changes.append("Semester updated; ");
                activityLogService.logUpdateActivity(
                        user,
                        "PROFILE_UPDATE_SEMESTER",
                        "USER",
                        "User " + user.getFullName() + " updated their semester",
                        "User",
                        user.getId(),
                        String.valueOf(oldProfile.getSemester()),
                        String.valueOf(request.getSemester()));
            }

            // Log general profile update if any changes were made
            if (changes.length() > 0) {
                activityLogService.logActivity(
                        user,
                        "PROFILE_UPDATE",
                        "USER",
                        "User " + user.getFullName() + " updated profile: " + changes.toString());
            }

            // Return updated profile
            UserProfileDTO profile = userService.getUserProfile(email);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get pending collaboration requests for user's projects
     */
    @GetMapping("/pending-requests")
    public ResponseEntity<List<PendingRequestDTO>> getPendingRequests() {
        String email = getCurrentUserEmail();
        User user = userService.getUserByEmail(email);

        // Get all projects owned by user
        List<PendingRequestDTO> pendingRequests = projectRepository.findByOwner(user)
                .stream()
                .flatMap(project -> contributionRequestRepository.findByProject(project).stream())
                .filter(req -> req.getStatus() == RequestStatus.PENDING)
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(pendingRequests);
    }

    /**
     * Approve a collaboration request
     */
    @PostMapping("/requests/{requestId}/approve")
    public ResponseEntity<?> approveRequest(@PathVariable Long requestId) {
        try {
            String email = getCurrentUserEmail();
            User user = userService.getUserByEmail(email);

            ContributionRequest request = contributionRequestRepository.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("Request not found"));

            // Verify the project belongs to current user
            if (!request.getProject().getOwner().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("You can only approve requests for your own projects");
            }

            request.setStatus(RequestStatus.APPROVED);
            contributionRequestRepository.save(request);

            // Log approval activity
            activityLogService.logEntityActivity(
                    user,
                    "COLLABORATION_REQUEST_APPROVED",
                    "COLLABORATION",
                    "User " + user.getFullName() + " approved collaboration request from " +
                            request.getRequestedBy().getFullName() + " for project '"
                            + request.getProject().getProjectName() + "'",
                    "ContributionRequest",
                    requestId);

            return ResponseEntity.ok("Request approved successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Reject a collaboration request
     */
    @PostMapping("/requests/{requestId}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable Long requestId) {
        try {
            String email = getCurrentUserEmail();
            User user = userService.getUserByEmail(email);

            ContributionRequest request = contributionRequestRepository.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("Request not found"));

            // Verify the project belongs to current user
            if (!request.getProject().getOwner().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("You can only reject requests for your own projects");
            }

            request.setStatus(RequestStatus.REJECTED);
            contributionRequestRepository.save(request);

            // Log rejection activity
            activityLogService.logEntityActivity(
                    user,
                    "COLLABORATION_REQUEST_REJECTED",
                    "COLLABORATION",
                    "User " + user.getFullName() + " rejected collaboration request from " +
                            request.getRequestedBy().getFullName() + " for project '"
                            + request.getProject().getProjectName() + "'",
                    "ContributionRequest",
                    requestId);

            return ResponseEntity.ok("Request rejected successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Change user password
     */
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody com.college.campuscollab.dto.ChangePasswordRequest request) {
        try {
            String email = getCurrentUserEmail();
            User user = userService.getUserByEmail(email);

            userService.changePassword(email, request.getCurrentPassword(), request.getNewPassword());

            // Log password change activity
            activityLogService.logActivity(
                    user,
                    "PASSWORD_CHANGE",
                    "USER",
                    "User " + user.getFullName() + " changed their password");

            return ResponseEntity.ok("Password changed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Helper methods

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        return authentication.getName();
    }

    private PendingRequestDTO convertToDTO(ContributionRequest request) {
        PendingRequestDTO dto = new PendingRequestDTO();
        dto.setId(request.getId());
        dto.setRequesterName(request.getRequestedBy().getFullName());
        dto.setRequesterEmail(request.getRequestedBy().getEmail());
        dto.setRequesterCourse(request.getRequestedBy().getCourse());
        dto.setRequesterSemester(request.getRequestedBy().getSemester());
        dto.setProjectName(request.getProject().getProjectName());
        dto.setProjectId(request.getProject().getId());
        dto.setMessage(request.getMessage());
        dto.setRequestedAt(request.getRequestedAt());
        dto.setStatus(request.getStatus().name());
        return dto;
    }
}
