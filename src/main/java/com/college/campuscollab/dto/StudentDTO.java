package com.college.campuscollab.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentDTO {
    private Long id;
    private String fullName;
    private String email;
    private String rollNumber;
    private String course;
    private Integer semester;
    private String role;
    private Integer projectCount;
    private String status; // active, inactive
    private LocalDateTime lastActive;
    private LocalDateTime joinDate;
}
