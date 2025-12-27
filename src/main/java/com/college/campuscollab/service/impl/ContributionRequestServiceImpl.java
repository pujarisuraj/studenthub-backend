package com.college.campuscollab.service.impl;

import com.college.campuscollab.dto.ContributionResponse;
import com.college.campuscollab.entity.ContributionRequest;
import com.college.campuscollab.entity.Project;
import com.college.campuscollab.entity.RequestStatus;
import com.college.campuscollab.entity.User;
import com.college.campuscollab.repository.ContributionRequestRepository;
import com.college.campuscollab.service.ContributionRequestService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContributionRequestServiceImpl implements ContributionRequestService {

    private final ContributionRequestRepository repository;

    public ContributionRequestServiceImpl(ContributionRequestRepository repository) {
        this.repository = repository;
    }

    @Override
    public ContributionRequest requestContribution(Project project, User user, String message) {
        ContributionRequest request = new ContributionRequest();
        request.setProject(project);
        request.setRequestedBy(user);
        request.setMessage(message);
        request.setStatus(RequestStatus.PENDING);
        request.setRequestedAt(LocalDateTime.now());
        return repository.save(request);
    }

    @Override
    public List<ContributionRequest> getRequestsForProject(Project project) {
        return repository.findByProject(project);
    }

    @Override
    public List<ContributionRequest> getRequestsByUser(User user) {
        return repository.findByRequestedBy(user);
    }

    @Override
    public ContributionRequest approveRequest(Long requestId) {
        ContributionRequest request = repository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Contribution request not found"));
        request.setStatus(RequestStatus.APPROVED);
        return repository.save(request);
    }

    @Override
    public ContributionRequest rejectRequest(Long requestId) {
        ContributionRequest request = repository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Contribution request not found"));
        request.setStatus(RequestStatus.REJECTED);
        return repository.save(request);
    }

    @Override
    public ContributionResponse convertToResponse(ContributionRequest request) {
        ContributionResponse response = new ContributionResponse();
        response.setId(request.getId());
        response.setProjectId(request.getProject().getId());
        response.setProjectName(request.getProject().getProjectName());
        response.setRequestedById(request.getRequestedBy().getId());
        response.setRequestedByName(request.getRequestedBy().getFullName());
        response.setRequestedByEmail(request.getRequestedBy().getEmail());
        response.setMessage(request.getMessage());
        response.setStatus(request.getStatus().name());
        response.setRequestedAt(request.getRequestedAt());
        return response;
    }

    @Override
    public boolean hasDownloadAccess(Long projectId, User user) {
        // Find the request for this project and user
        ContributionRequest request = repository.findByProjectAndRequestedBy(
                new Project() {
                    {
                        setId(projectId);
                    }
                },
                user);

        // User has access if they have an approved request
        return request != null && request.getStatus() == RequestStatus.APPROVED;
    }
}
