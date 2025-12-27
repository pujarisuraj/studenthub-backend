package com.college.campuscollab.repository;

import com.college.campuscollab.entity.ProblemSubmission;
import com.college.campuscollab.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ProblemSubmissionRepository
                extends JpaRepository<ProblemSubmission, Long> {

        List<ProblemSubmission> findBySubmissionDateBetween(
                        LocalDate start, LocalDate end);

        List<ProblemSubmission> findByUser(User user);

        // Delete all problem submissions by a user
        void deleteByUser(User user);
}
