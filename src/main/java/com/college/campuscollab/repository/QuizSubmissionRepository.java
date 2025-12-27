package com.college.campuscollab.repository;

import com.college.campuscollab.entity.QuizSubmission;
import com.college.campuscollab.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuizSubmissionRepository
        extends JpaRepository<QuizSubmission, Long> {
    @Query("""
                SELECT qs.user.email, SUM(qs.score)
                FROM QuizSubmission qs
                GROUP BY qs.user.email
                ORDER BY SUM(qs.score) DESC
            """)
    List<Object[]> getQuizLeaderboard();

    // Delete all quiz submissions by a user
    void deleteByUser(User user);
}
