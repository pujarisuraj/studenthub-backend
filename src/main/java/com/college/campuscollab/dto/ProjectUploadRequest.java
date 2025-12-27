package com.college.campuscollab.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectUploadRequest {
    private String projectName;
    private String teamLeaderName;
    private String course;
    private Integer semester;
    private String techStack;
    private String description;

    private String liveLink;
    private String codeLink;
}
