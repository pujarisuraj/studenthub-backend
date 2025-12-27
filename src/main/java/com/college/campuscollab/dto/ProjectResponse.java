package com.college.campuscollab.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Project API responses
 * Includes project details along with owner information
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private Long id;
    private String projectName;
    private String teamLeaderName;
    private String course;
    private Integer semester;
    private String techStack;
    private String description;
    private String liveLink;
    private String codeLink;
    private String status;
    private List<String> screenshots;
    private Integer viewCount;
    private Integer likeCount;
    private LocalDateTime createdAt;

    // Owner information
    private OwnerInfo owner;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerInfo {
        private Long id;
        private String name;
        private String email;
        private String role;
    }
}
