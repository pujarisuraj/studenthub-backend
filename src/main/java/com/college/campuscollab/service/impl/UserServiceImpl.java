package com.college.campuscollab.service.impl;

import com.college.campuscollab.dto.RegisterRequest;
import com.college.campuscollab.dto.UpdateProfileRequest;
import com.college.campuscollab.dto.UserProfileDTO;
import com.college.campuscollab.entity.RequestStatus;
import com.college.campuscollab.entity.Role;
import com.college.campuscollab.entity.User;
import com.college.campuscollab.repository.ContributionRequestRepository;
import com.college.campuscollab.repository.ProjectRepository;
import com.college.campuscollab.repository.UserRepository;
import com.college.campuscollab.service.ActivityLogService;
import com.college.campuscollab.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProjectRepository projectRepository;
    private final ContributionRequestRepository contributionRequestRepository;
    private final ActivityLogService activityLogService;

    public UserServiceImpl(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ProjectRepository projectRepository,
            ContributionRequestRepository contributionRequestRepository,
            ActivityLogService activityLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.projectRepository = projectRepository;
        this.contributionRequestRepository = contributionRequestRepository;
        this.activityLogService = activityLogService;
    }

    @Override
    public User registerUser(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        if (userRepository.existsByRollNumber(request.getRollNumber())) {
            throw new RuntimeException("Roll number already registered");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setRollNumber(request.getRollNumber());
        user.setCourse(request.getCourse());
        user.setSemester(request.getSemester());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // AUTO ROLE ASSIGNMENT LOGIC
        if (request.getSemester() != null && request.getSemester() >= 5) {
            user.setRole(Role.SENIOR);
        } else {
            user.setRole(Role.STUDENT);
        }

        userRepository.save(user);
        return user;
    }

    @Override
    public User registerAdmin(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User admin = new User();
        admin.setFullName(request.getFullName());
        admin.setEmail(request.getEmail());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));

        // Set admin role
        admin.setRole(Role.ADMIN);

        // Admin doesn't need roll number, course, semester - set default values
        admin.setRollNumber("ADMIN");
        admin.setCourse("N/A");
        admin.setSemester(0);

        userRepository.save(admin);
        return admin;
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public UserProfileDTO getUserProfile(String email) {
        User user = getUserByEmail(email);

        // Count projects uploaded by user
        int projectsUploaded = projectRepository.findByOwner(user).size();

        // Count approved contribution requests (collaborations)
        int projectsCollaborated = (int) contributionRequestRepository
                .findByRequestedBy(user)
                .stream()
                .filter(req -> req.getStatus() == RequestStatus.APPROVED)
                .count();

        // Count pending requests for user's projects
        int pendingRequests = (int) projectRepository.findByOwner(user)
                .stream()
                .flatMap(project -> contributionRequestRepository.findByProject(project).stream())
                .filter(req -> req.getStatus() == RequestStatus.PENDING)
                .count();

        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setId(user.getId());
        profileDTO.setFullName(user.getFullName());
        profileDTO.setEmail(user.getEmail());
        profileDTO.setRollNumber(user.getRollNumber());
        profileDTO.setCourse(user.getCourse());
        profileDTO.setSemester(user.getSemester());
        profileDTO.setRole(user.getRole().name());
        profileDTO.setAccountStatus(user.getAccountStatus() != null ? user.getAccountStatus().name() : "ACTIVE");
        profileDTO.setProjectsUploaded(projectsUploaded);
        profileDTO.setProjectsCollaborated(projectsCollaborated);
        profileDTO.setPendingRequests(pendingRequests);

        return profileDTO;
    }

    @Override
    public User updateUserProfile(String email, UpdateProfileRequest request) {
        User user = getUserByEmail(email);

        // Build description of changes for activity log
        StringBuilder changesDescription = new StringBuilder("Updated profile: ");
        boolean hasChanges = false;

        if (request.getFullName() != null && !request.getFullName().isEmpty()) {
            if (!request.getFullName().equals(user.getFullName())) {
                changesDescription.append(String.format("Name (%s → %s), ",
                        user.getFullName(), request.getFullName()));
                hasChanges = true;
            }
            user.setFullName(request.getFullName());
        }

        if (request.getRollNumber() != null && !request.getRollNumber().isEmpty()) {
            // Check if roll number is already taken by another user
            if (!request.getRollNumber().equals(user.getRollNumber())
                    && userRepository.existsByRollNumber(request.getRollNumber())) {
                throw new RuntimeException("Roll number already in use");
            }
            if (!request.getRollNumber().equals(user.getRollNumber())) {
                changesDescription.append(String.format("Roll Number (%s → %s), ",
                        user.getRollNumber(), request.getRollNumber()));
                hasChanges = true;
            }
            user.setRollNumber(request.getRollNumber());
        }

        if (request.getCourse() != null && !request.getCourse().isEmpty()) {
            if (!request.getCourse().equals(user.getCourse())) {
                changesDescription.append(String.format("Course (%s → %s), ",
                        user.getCourse(), request.getCourse()));
                hasChanges = true;
            }
            user.setCourse(request.getCourse());
        }

        if (request.getSemester() != null) {
            if (!request.getSemester().equals(user.getSemester())) {
                changesDescription.append(String.format("Semester (%s → %s), ",
                        user.getSemester(), request.getSemester()));
                hasChanges = true;
            }
            user.setSemester(request.getSemester());

            // Update role based on semester
            Role oldRole = user.getRole();
            if (request.getSemester() >= 5) {
                user.setRole(Role.SENIOR);
            } else {
                user.setRole(Role.STUDENT);
            }

            // Log role upgrade if it changed
            if (oldRole != user.getRole()) {
                changesDescription.append(String.format("Role upgraded (%s → %s), ",
                        oldRole, user.getRole()));
                hasChanges = true;
            }
        }

        User updatedUser = userRepository.save(user);

        // Log the activity if there were any changes
        if (hasChanges) {
            // Remove trailing comma and space
            String finalDescription = changesDescription.toString().replaceAll(", $", "");

            try {
                activityLogService.logEntityActivity(
                        updatedUser,
                        "PROFILE_UPDATE",
                        "USER",
                        finalDescription,
                        "User",
                        updatedUser.getId());
            } catch (Exception e) {
                // Don't fail the update if logging fails
                System.err.println("Failed to log profile update activity: " + e.getMessage());
            }
        }

        return updatedUser;
    }

    @Override
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = getUserByEmail(email);

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Encode and set new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
