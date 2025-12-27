package com.college.campuscollab.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingRequestDTO {

    private Long id;
    private String requesterName;
    private String requesterEmail;
    private String requesterCourse;
    private Integer requesterSemester;
    private String projectName;
    private Long projectId;
    private String message;
    private LocalDateTime requestedAt;
    private String status;
}
