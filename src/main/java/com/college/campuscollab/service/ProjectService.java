package com.college.campuscollab.service;

import com.college.campuscollab.dto.ProjectResponse;
import com.college.campuscollab.dto.ProjectUploadRequest;
import com.college.campuscollab.entity.Project;
import com.college.campuscollab.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProjectService {

    void uploadProject(ProjectUploadRequest request,
            List<MultipartFile> files,
            User user);

    List<Project> getAllProjects();

    List<Project> getProjectsByOwner(User owner);

    // New methods for enhanced GET APIs
    List<ProjectResponse> getAllProjectsAsResponse();

    List<ProjectResponse> getApprovedProjects();

    List<ProjectResponse> searchProjects(String searchTerm, String status, String course, String techStack);

    ProjectResponse getProjectById(Long id);

    // Get Project entity (not DTO) - for internal use
    Project getProjectEntityById(Long id);

    void incrementViewCount(Long projectId);

    void toggleLike(Long projectId, User user);

    boolean hasUserLiked(Long projectId, User user);

    // Convert Project entity to ProjectResponse DTO
    ProjectResponse convertToResponse(Project project);
}
