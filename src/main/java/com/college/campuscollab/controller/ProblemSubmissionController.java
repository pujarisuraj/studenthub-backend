package com.college.campuscollab.controller;

import com.college.campuscollab.entity.ProblemSubmission;
import com.college.campuscollab.entity.User;
import com.college.campuscollab.service.ProblemSubmissionService;
import com.college.campuscollab.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/problems")
public class ProblemSubmissionController {

    private final ProblemSubmissionService service;
    private final UserService userService;

    public ProblemSubmissionController(
            ProblemSubmissionService service,
            UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    //  Submit problem
    @PostMapping("/submit")
    public ProblemSubmission submitProblem(
            @RequestBody ProblemSubmission submission,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user =
                userService.getUserByEmail(userDetails.getUsername());

        return service.submitProblem(submission, user);
    }

    //  Weekly leaderboard
    @GetMapping("/leaderboard/weekly")
    public Map<String, Integer> weeklyLeaderboard() {

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(7);

        return service.getLeaderboard(start, end);
    }

    //  Monthly leaderboard
    @GetMapping("/leaderboard/monthly")
    public Map<String, Integer> monthlyLeaderboard() {

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(30);

        return service.getLeaderboard(start, end);
    }
}
