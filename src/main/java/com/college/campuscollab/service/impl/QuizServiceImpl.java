package com.college.campuscollab.service.impl;

import com.college.campuscollab.entity.*;
import com.college.campuscollab.repository.*;
import com.college.campuscollab.service.QuizService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuizSubmissionRepository submissionRepository;

    public QuizServiceImpl(QuizRepository quizRepository,
                           QuizSubmissionRepository submissionRepository) {
        this.quizRepository = quizRepository;
        this.submissionRepository = submissionRepository;
    }

    @Override
    public Quiz createQuiz(Quiz quiz) {

        quiz.setCreatedDate(LocalDate.now());

        if (quiz.getQuestions() != null) {
            for (QuizQuestion q : quiz.getQuestions()) {
                q.setQuiz(quiz);
            }
        }

        return quizRepository.save(quiz);
    }


    @Override
    public int submitQuiz(Long quizId,
                          Map<Long, String> answers,
                          User user) {

        Quiz quiz = quizRepository.findById(quizId).orElseThrow();
        int score = 0;

        for (QuizQuestion q : quiz.getQuestions()) {

            //Get submitted answer for this question
            String submitted = answers.get(q.getId());
            if (submitted != null &&
                    q.getCorrectOption() != null &&
                    q.getCorrectOption()
                            .trim()
                            .equalsIgnoreCase(submitted.trim())) {

                score++;
            }
        }

        QuizSubmission submission = new QuizSubmission();
        submission.setQuiz(quiz);
        submission.setUser(user);
        submission.setScore(score);

        submissionRepository.save(submission);
        return score;
    }
    @Override
    public Map<String, Integer> getQuizLeaderboard() {

        Map<String, Integer> leaderboard = new LinkedHashMap<>();

        List<Object[]> results = submissionRepository.getQuizLeaderboard();

        for (Object[] row : results) {
            String email = (String) row[0];
            Integer totalScore = ((Long) row[1]).intValue();
            leaderboard.put(email, totalScore);
        }

        return leaderboard;
    }
}
