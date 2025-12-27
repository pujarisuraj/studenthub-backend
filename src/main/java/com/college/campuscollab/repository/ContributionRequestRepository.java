package com.college.campuscollab.repository;

import com.college.campuscollab.entity.ContributionRequest;
import com.college.campuscollab.entity.Project;
import com.college.campuscollab.entity.RequestStatus;
import com.college.campuscollab.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContributionRequestRepository extends JpaRepository<ContributionRequest, Long> {

    List<ContributionRequest> findByProject(Project project);

    List<ContributionRequest> findByRequestedBy(User user);

    // Find a specific request by project and user
    ContributionRequest findByProjectAndRequestedBy(Project project, User user);

    // Count requests by status
    long countByStatus(RequestStatus status);

    // Find requests by status
    List<ContributionRequest> findByStatus(RequestStatus status);

    // Count requests by user and status (for leaderboard)
    long countByRequestedByAndStatus(User user, RequestStatus status);
}
