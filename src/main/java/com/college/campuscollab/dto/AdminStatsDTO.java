package com.college.campuscollab.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminStatsDTO {
    private long totalStudents;
    private long totalProjects;
    private long pendingRequests;
    private long activeCollaborations; // Approved requests
}
