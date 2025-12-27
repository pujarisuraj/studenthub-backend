package com.college.campuscollab.service;

import com.college.campuscollab.dto.RegisterRequest;
import com.college.campuscollab.dto.UpdateProfileRequest;
import com.college.campuscollab.dto.UserProfileDTO;
import com.college.campuscollab.entity.User;

public interface UserService {

    User registerUser(RegisterRequest user);

    User registerAdmin(RegisterRequest request);

    User getUserByEmail(String email);

    User getUserById(Long id);

    UserProfileDTO getUserProfile(String email);

    User updateUserProfile(String email, UpdateProfileRequest request);

    void changePassword(String email, String currentPassword, String newPassword);

}
