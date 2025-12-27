package com.college.campuscollab.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminProjectDTO {
    private Long id;
    private String projectName;
    private String teamLeaderName;
    private String ownerName;
    private String ownerEmail;
    private String course;
    private Integer semester;
    private String techStack;
    private String description;
    private String liveLink;
    private String codeLink;
    private String status;
    private Integer viewCount;
    private Integer likeCount;
    private List<String> screenshots;
    private LocalDateTime createdAt;
}
