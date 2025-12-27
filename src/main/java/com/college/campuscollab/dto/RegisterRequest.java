package com.college.campuscollab.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String fullName;
    private String email;
    private String rollNumber;
    private String course;
    private Integer semester;
    private String password;
    private String confirmPassword;

}
