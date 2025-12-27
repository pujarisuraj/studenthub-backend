package com.college.campuscollab.controller;

import com.college.campuscollab.dto.ContributionRequestDTO;
import com.college.campuscollab.dto.ContributionResponse;
import com.college.campuscollab.entity.ContributionRequest;
import com.college.campuscollab.entity.Project;
import com.college.campuscollab.entity.User;
import com.college.campuscollab.service.ContributionRequestService;
import com.college.campuscollab.service.ProjectService;
import com.college.campuscollab.service.UserService;
import com.college.campuscollab.service.ActivityLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contributions")
public class ContributionController {

    private final ContributionRequestService contributionService;
    private final ProjectService projectService;
    private final UserService userService;
    private final ActivityLogService activityLogService;

    public ContributionController(ContributionRequestService contributionService,
            ProjectService projectService,
            UserService userService,
            ActivityLogService activityLogService) {
        this.contributionService = contributionService;
        this.projectService = projectService;
        this.userService = userService;
        this.activityLogService = activityLogService;
    }

    /**
     * Submit a contribution request to a project
     * Authentication: Required
     */
    @PostMapping("/{projectId}")
    public ResponseEntity<Map<String, Object>> requestContribution(
            @PathVariable Long projectId,
            @RequestBody ContributionRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();

        try {
            User user = userService.getUserByEmail(userDetails.getUsername());

            // Use efficient getProjectEntityById instead of getAllProjects() + filter
            Project project = projectService.getProjectEntityById(projectId);

            ContributionRequest request = contributionService.requestContribution(
                    project, user, requestDTO.getMessage());

            // Log contribution request activity
            activityLogService.logEntityActivity(
                    user,
                    "CONTRIBUTION_REQUEST_CREATED",
                    "COLLABORATION",
                    "User " + user.getFullName() + " requested collaboration for project '" + project.getProjectName()
                            + "'",
                    "ContributionRequest",
                    request.getId());

            ContributionResponse contributionResponse = contributionService.convertToResponse(request);

            response.put("success", true);
            response.put("message", "Contribution request submitted successfully");
            response.put("data", contributionResponse);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Get all contribution requests for a specific project (Owner only)
     * Authentication: Required (must be project owner)
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<Map<String, Object>> getRequestsForProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();

        try {
            User loggedInUser = userService.getUserByEmail(userDetails.getUsername());
            Project project = projectService.getProjectEntityById(projectId);

            // Owner check
            if (!project.getOwner().getId().equals(loggedInUser.getId())) {
                response.put("success", false);
                response.put("message", "Access denied: You are not the owner of this project");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            List<ContributionResponse> requests = contributionService.getRequestsForProject(project)
                    .stream()
                    .map(contributionService::convertToResponse)
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("data", requests);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Get all contribution requests made by the authenticated user
     * Authentication: Required
     */
    @GetMapping("/my-requests")
    public ResponseEntity<List<ContributionResponse>> getMyRequests(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.getUserByEmail(userDetails.getUsername());

        List<ContributionResponse> requests = contributionService.getRequestsByUser(user)
                .stream()
                .map(contributionService::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(requests);
    }

    /**
     * Approve a contribution request (Owner only)
     * Authentication: Required (must be project owner)
     */
    @PutMapping("/{requestId}/approve")
    public ResponseEntity<Map<String, Object>> approveRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();

        try {
            User loggedInUser = userService.getUserByEmail(userDetails.getUsername());

            // Get the request first to check ownership
            ContributionRequest request = contributionService.approveRequest(requestId);

            // Verify the logged-in user is the project owner
            if (!request.getProject().getOwner().getId().equals(loggedInUser.getId())) {
                response.put("success", false);
                response.put("message", "Access denied: You are not the owner of this project");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            ContributionResponse contributionResponse = contributionService.convertToResponse(request);

            response.put("success", true);
            response.put("message", "Contribution request approved");
            response.put("data", contributionResponse);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Reject a contribution request (Owner only)
     * Authentication: Required (must be project owner)
     */
    @PutMapping("/{requestId}/reject")
    public ResponseEntity<Map<String, Object>> rejectRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();

        try {
            User loggedInUser = userService.getUserByEmail(userDetails.getUsername());

            // Get the request first to check ownership
            ContributionRequest request = contributionService.rejectRequest(requestId);

            // Verify the logged-in user is the project owner
            if (!request.getProject().getOwner().getId().equals(loggedInUser.getId())) {
                response.put("success", false);
                response.put("message", "Access denied: You are not the owner of this project");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            ContributionResponse contributionResponse = contributionService.convertToResponse(request);

            response.put("success", true);
            response.put("message", "Contribution request rejected");
            response.put("data", contributionResponse);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Check if the current user has access to download a project's code
     * Returns: "owner", "approved", "pending", "rejected", or "no_request"
     * Authentication: Required
     */
    @GetMapping("/access/{projectId}")
    public ResponseEntity<Map<String, Object>> checkDownloadAccess(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();

        try {
            User loggedInUser = userService.getUserByEmail(userDetails.getUsername());
            Project project = projectService.getProjectEntityById(projectId);

            // Check if user is the owner
            if (project.getOwner().getId().equals(loggedInUser.getId())) {
                response.put("success", true);
                response.put("status", "owner");
                response.put("hasAccess", true);
                response.put("message", "You are the project owner");
                return ResponseEntity.ok(response);
            }

            // Check if user has a contribution request
            ContributionRequest request = contributionService.getRequestsForProject(project)
                    .stream()
                    .filter(r -> r.getRequestedBy().getId().equals(loggedInUser.getId()))
                    .findFirst()
                    .orElse(null);

            if (request == null) {
                response.put("success", true);
                response.put("status", "no_request");
                response.put("hasAccess", false);
                response.put("message", "No download request submitted");
                return ResponseEntity.ok(response);
            }

            // Check request status
            String status = request.getStatus().name().toLowerCase();
            boolean hasAccess = "approved".equals(status);

            response.put("success", true);
            response.put("status", status);
            response.put("hasAccess", hasAccess);
            response.put("requestId", request.getId());

            if (hasAccess) {
                response.put("message", "Download access granted");
            } else if ("pending".equals(status)) {
                response.put("message", "Download request is pending approval");
            } else {
                response.put("message", "Download request was rejected");
            }

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
