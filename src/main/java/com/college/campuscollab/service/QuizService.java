package com.college.campuscollab.service;

import com.college.campuscollab.entity.Quiz;
import com.college.campuscollab.entity.User;

import java.util.Map;

public interface QuizService {

    Quiz createQuiz(Quiz quiz);

    int submitQuiz(Long quizId,
                   Map<Long, String> answers,
                   User user);
    Map<String, Integer> getQuizLeaderboard();
}
