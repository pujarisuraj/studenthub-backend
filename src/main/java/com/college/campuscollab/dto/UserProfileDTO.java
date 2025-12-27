package com.college.campuscollab.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {

    private Long id;
    private String fullName;
    private String email;
    private String rollNumber;
    private String course;
    private Integer semester;
    private String role;
    private String accountStatus; // ACTIVE, SUSPENDED, INACTIVE
    private Integer projectsUploaded;
    private Integer projectsCollaborated;
    private Integer pendingRequests;
}
