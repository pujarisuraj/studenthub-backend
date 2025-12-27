package com.college.campuscollab.controller;

import com.college.campuscollab.dto.ProjectResponse;
import com.college.campuscollab.dto.ProjectUploadRequest;
import com.college.campuscollab.entity.Project;
import com.college.campuscollab.entity.User;
import com.college.campuscollab.service.ProjectService;
import com.college.campuscollab.service.UserService;
import com.college.campuscollab.service.ActivityLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:3001" }, allowCredentials = "true")
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;
    private final ActivityLogService activityLogService;

    public ProjectController(ProjectService projectService,
            UserService userService,
            ActivityLogService activityLogService) {
        this.projectService = projectService;
        this.userService = userService;
        this.activityLogService = activityLogService;
    }

    /**
     * Upload a new project with screenshots
     * This endpoint accepts multipart/form-data with:
     * - Form fields for project data (projectName, teamLeaderName, course, etc.)
     * - File attachments for screenshots
     * 
     * Authentication: Requires valid JWT token in Authorization header
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadProject(
            @ModelAttribute ProjectUploadRequest request,
            @RequestParam(value = "screenshots", required = false) List<MultipartFile> screenshots,
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate user authentication
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Get authenticated user
            User user = userService.getUserByEmail(userDetails.getUsername());

            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Validate project data
            if (request.getProjectName() == null || request.getProjectName().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Project name is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Handle screenshots (can be empty list)
            List<MultipartFile> files = (screenshots != null) ? screenshots : List.of();

            // Upload project
            projectService.uploadProject(request, files, user);

            // Log project upload activity
            activityLogService.logActivity(
                    user,
                    "PROJECT_UPLOAD",
                    "PROJECT",
                    "User " + user.getFullName() + " uploaded project '" + request.getProjectName() + "'");

            response.put("success", true);
            response.put("message", "Project uploaded successfully");
            response.put("projectName", request.getProjectName());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to upload project: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get all projects with full details (for browsing)
     * Returns projects ordered by creation date (newest first)
     * Authentication: Optional (public endpoint)
     */
    @GetMapping("/browse")
    public ResponseEntity<List<ProjectResponse>> browseProjects() {
        List<ProjectResponse> projects = projectService.getAllProjectsAsResponse();
        return ResponseEntity.ok(projects);
    }

    /**
     * Get approved projects only (for public display)
     * Authentication: Optional (public endpoint)
     */
    @GetMapping("/approved")
    public ResponseEntity<List<ProjectResponse>> getApprovedProjects() {
        List<ProjectResponse> projects = projectService.getApprovedProjects();
        return ResponseEntity.ok(projects);
    }

    /**
     * Search and filter projects
     * Query params:
     * - search: Search in name, description, tech stack
     * - status: Filter by status (PENDING, APPROVED, REJECTED)
     * - course: Filter by course name
     * - techStack: Filter by technology stack
     * 
     * Authentication: Optional (public endpoint)
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProjectResponse>> searchProjects(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String course,
            @RequestParam(required = false) String techStack) {

        List<ProjectResponse> projects = projectService.searchProjects(search, status, course, techStack);
        return ResponseEntity.ok(projects);
    }

    /**
     * Get single project by ID
     * Increments view count automatically
     * Authentication: Optional (public endpoint)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long id) {
        try {
            // Increment view count
            projectService.incrementViewCount(id);

            // Get and return project
            ProjectResponse project = projectService.getProjectById(id);
            return ResponseEntity.ok(project);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Toggle like on a project
     * Authentication: Required
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            User user = userService.getUserByEmail(userDetails.getUsername());
            projectService.toggleLike(id, user);

            // Get updated project to return new like count
            ProjectResponse project = projectService.getProjectById(id);

            // Log like activity
            activityLogService.logEntityActivity(
                    user,
                    "PROJECT_LIKE",
                    "PROJECT",
                    "User " + user.getFullName() + " liked project '" + project.getProjectName() + "'",
                    "Project",
                    id);

            response.put("success", true);
            response.put("message", "Like toggled successfully");
            response.put("likeCount", project.getLikeCount());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // Legacy endpoints (keeping for backward compatibility)

    /**
     * @deprecated Use /browse or /approved instead
     */
    @GetMapping
    public List<Project> getAllProjects() {
        return projectService.getAllProjects();
    }

    /**
     * Get projects uploaded by the authenticated user
     * Authentication: Required
     */
    @GetMapping("/my")
    public List<Project> getMyProjects(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.getUserByEmail(userDetails.getUsername());
        return projectService.getProjectsByOwner(user);
    }
}
