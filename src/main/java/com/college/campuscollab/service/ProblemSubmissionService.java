package com.college.campuscollab.service;

import com.college.campuscollab.entity.ProblemSubmission;
import com.college.campuscollab.entity.User;

import java.time.LocalDate;
import java.util.Map;

public interface ProblemSubmissionService {

    ProblemSubmission submitProblem(
            ProblemSubmission submission, User user);

    Map<String, Integer> getLeaderboard(
            LocalDate start, LocalDate end);
}
