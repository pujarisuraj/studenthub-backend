package com.college.campuscollab.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {

    private String fullName;
    private String email;
    private String rollNumber;
    private String course;
    private Integer semester;
    private String status; // ACTIVE, SUSPENDED, INACTIVE
}
