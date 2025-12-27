package com.college.campuscollab.service;

import com.college.campuscollab.dto.ContributionResponse;
import com.college.campuscollab.entity.ContributionRequest;
import com.college.campuscollab.entity.Project;
import com.college.campuscollab.entity.User;

import java.util.List;

public interface ContributionRequestService {

    ContributionRequest requestContribution(Project project, User user, String message);

    List<ContributionRequest> getRequestsForProject(Project project);

    List<ContributionRequest> getRequestsByUser(User user);

    ContributionRequest approveRequest(Long requestId);

    ContributionRequest rejectRequest(Long requestId);

    ContributionResponse convertToResponse(ContributionRequest request);

    // Check if user has access to download project (owner or approved request)
    boolean hasDownloadAccess(Long projectId, User user);
}
