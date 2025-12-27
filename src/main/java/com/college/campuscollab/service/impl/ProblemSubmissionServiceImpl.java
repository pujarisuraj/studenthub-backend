package com.college.campuscollab.service.impl;

import com.college.campuscollab.entity.ProblemSubmission;
import com.college.campuscollab.entity.User;
import com.college.campuscollab.repository.ProblemSubmissionRepository;
import com.college.campuscollab.service.ProblemSubmissionService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProblemSubmissionServiceImpl
        implements ProblemSubmissionService {

    private final ProblemSubmissionRepository repository;

    public ProblemSubmissionServiceImpl(
            ProblemSubmissionRepository repository) {
        this.repository = repository;
    }

    @Override
    public ProblemSubmission submitProblem(
            ProblemSubmission submission, User user) {

        submission.setUser(user);
        submission.setSubmissionDate(LocalDate.now());

        switch (submission.getDifficulty().toUpperCase()) {
            case "EASY" -> submission.setPoints(10);
            case "MEDIUM" -> submission.setPoints(20);
            case "HARD" -> submission.setPoints(30);
            default -> submission.setPoints(0);
        }

        return repository.save(submission);
    }

    @Override
    public Map<String, Integer> getLeaderboard(
            LocalDate start, LocalDate end) {

        List<ProblemSubmission> submissions =
                repository.findBySubmissionDateBetween(start, end);

        Map<String, Integer> leaderboard = new HashMap<>();

        for (ProblemSubmission ps : submissions) {
            String email = ps.getUser().getEmail();
            leaderboard.put(
                    email,
                    leaderboard.getOrDefault(email, 0) + ps.getPoints()
            );
        }

        return leaderboard;
    }
}
