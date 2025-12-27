package com.college.campuscollab.dto;

import lombok.Getter;

@Getter
public class LoginResponse {
    private String token;
    private String email;
    private String role;

    public LoginResponse(String token, String email, String role) {
        this.token = token;
        this.email = email;
        this.role = role;
    }
}
