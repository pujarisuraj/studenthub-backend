package com.college.campuscollab.service.impl;

import com.college.campuscollab.dto.ProjectResponse;
import com.college.campuscollab.dto.ProjectUploadRequest;
import com.college.campuscollab.entity.Project;
import com.college.campuscollab.entity.ProjectLike;
import com.college.campuscollab.entity.User;
import com.college.campuscollab.repository.ProjectLikeRepository;
import com.college.campuscollab.repository.ProjectRepository;
import com.college.campuscollab.service.ProjectService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final FileStorageServiceImpl fileStorageService;
    private final ProjectLikeRepository projectLikeRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository,
            FileStorageServiceImpl fileStorageService,
            ProjectLikeRepository projectLikeRepository) {
        this.projectRepository = projectRepository;
        this.fileStorageService = fileStorageService;
        this.projectLikeRepository = projectLikeRepository;
    }

    @Override
    public void uploadProject(ProjectUploadRequest request,
            List<MultipartFile> files,
            User user) {

        Project project = new Project();
        project.setProjectName(request.getProjectName());
        project.setTeamLeaderName(request.getTeamLeaderName());
        project.setCourse(request.getCourse());
        project.setSemester(request.getSemester());
        project.setTechStack(request.getTechStack());
        project.setDescription(request.getDescription());
        project.setLiveLink(request.getLiveLink());
        project.setCodeLink(request.getCodeLink());
        project.setStatus("PENDING");

        project.setOwner(user);

        List<String> screenshotPaths = new ArrayList<>();

        for (MultipartFile file : files) {
            String path = fileStorageService.save(file); // local or cloud
            screenshotPaths.add(path);
        }

        project.setScreenshots(screenshotPaths);

        projectRepository.save(project);
    }

    @Override
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @Override
    public List<Project> getProjectsByOwner(User owner) {
        return projectRepository.findByOwner(owner);
    }

    @Override
    public List<ProjectResponse> getAllProjectsAsResponse() {
        return projectRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectResponse> getApprovedProjects() {
        return projectRepository.findByStatusOrderByCreatedAtDesc("APPROVED")
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectResponse> searchProjects(String searchTerm, String status, String course, String techStack) {
        return projectRepository.findProjectsWithFilters(status, course, techStack, searchTerm)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
        return convertToResponse(project);
    }

    @Override
    public Project getProjectEntityById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
    }

    @Override
    public void incrementViewCount(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));
        project.setViewCount(project.getViewCount() + 1);
        projectRepository.save(project);
    }

    @Override
    public void toggleLike(Long projectId, User user) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        // Check if user has already liked this project
        boolean alreadyLiked = projectLikeRepository.existsByProjectAndUser(project, user);

        if (alreadyLiked) {
            // Unlike: Remove the like
            ProjectLike like = projectLikeRepository.findByProjectAndUser(project, user)
                    .orElseThrow(() -> new RuntimeException("Like not found"));
            projectLikeRepository.delete(like);

            // Decrement like count
            project.setLikeCount(Math.max(0, project.getLikeCount() - 1));
        } else {
            // Like: Create new like
            ProjectLike newLike = new ProjectLike();
            newLike.setProject(project);
            newLike.setUser(user);
            projectLikeRepository.save(newLike);

            // Increment like count
            project.setLikeCount(project.getLikeCount() + 1);
        }

        projectRepository.save(project);
    }

    @Override
    public boolean hasUserLiked(Long projectId, User user) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));
        return projectLikeRepository.existsByProjectAndUser(project, user);
    }

    @Override
    public ProjectResponse convertToResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setProjectName(project.getProjectName());
        response.setTeamLeaderName(project.getTeamLeaderName());
        response.setCourse(project.getCourse());
        response.setSemester(project.getSemester());
        response.setTechStack(project.getTechStack());
        response.setDescription(project.getDescription());
        response.setLiveLink(project.getLiveLink());
        response.setCodeLink(project.getCodeLink());
        response.setStatus(project.getStatus());

        // Convert screenshot filenames to full URLs
        List<String> screenshotUrls = new ArrayList<>();
        if (project.getScreenshots() != null) {
            for (String filename : project.getScreenshots()) {
                // If it's already a full path (old data), extract just the filename
                String cleanFilename = filename;
                if (filename.contains("\\") || filename.contains("/")) {
                    cleanFilename = filename.substring(filename.lastIndexOf(File.separatorChar) + 1);
                    if (cleanFilename.equals(filename)) {
                        cleanFilename = filename.substring(filename.lastIndexOf('/') + 1);
                    }
                }
                // Create full URL: http://localhost:8085/api/files/projects/filename.jpg
                String url = "http://localhost:8085/api/files/projects/" + cleanFilename;
                screenshotUrls.add(url);
            }
        }
        response.setScreenshots(screenshotUrls);

        response.setViewCount(project.getViewCount());
        response.setLikeCount(project.getLikeCount());
        response.setCreatedAt(project.getCreatedAt());

        // Set owner information
        if (project.getOwner() != null) {
            ProjectResponse.OwnerInfo ownerInfo = new ProjectResponse.OwnerInfo();
            ownerInfo.setId(project.getOwner().getId());
            ownerInfo.setName(project.getOwner().getFullName());
            ownerInfo.setEmail(project.getOwner().getEmail());
            ownerInfo.setRole(project.getOwner().getRole() != null ? project.getOwner().getRole().name() : null);
            response.setOwner(ownerInfo);
        }

        return response;
    }
}
