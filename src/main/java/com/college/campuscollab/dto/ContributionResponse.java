package com.college.campuscollab.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContributionResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private Long requestedById;
    private String requestedByName;
    private String requestedByEmail;
    private String message;
    private String status;
    private LocalDateTime requestedAt;
}
