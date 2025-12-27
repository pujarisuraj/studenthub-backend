package com.college.campuscollab.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminRequestDTO {
    private Long id;
    private Long projectId;
    private String projectName;
    private String ownerName;
    private String ownerEmail;
    private String requesterName;
    private String requesterEmail;
    private String message;
    private String status;
    private LocalDateTime requestedAt;
}
