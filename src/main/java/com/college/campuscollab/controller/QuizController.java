package com.college.campuscollab.controller;

import com.college.campuscollab.entity.Quiz;
import com.college.campuscollab.entity.User;
import com.college.campuscollab.service.QuizService;
import com.college.campuscollab.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;
    private final UserService userService;

    public QuizController(QuizService quizService,
                          UserService userService) {
        this.quizService = quizService;
        this.userService = userService;
    }

    //  Create quiz (volunteer)
    @PostMapping
    public Quiz createQuiz(@RequestBody Quiz quiz) {
        return quizService.createQuiz(quiz);
    }

    //  Attempt quiz
    @PostMapping("/{quizId}/submit")
    public int submitQuiz(@PathVariable Long quizId,
                          @RequestBody Map<Long, String> answers,
                          @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.getUserByEmail(userDetails.getUsername());
        return quizService.submitQuiz(quizId, answers, user);
    }
    @GetMapping("/leaderboard")
    public Map<String, Integer> quizLeaderboard() {
        return quizService.getQuizLeaderboard();
    }

}
