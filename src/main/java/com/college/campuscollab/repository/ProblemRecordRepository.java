package com.college.campuscollab.repository;

import com.college.campuscollab.entity.ProblemRecord;
import com.college.campuscollab.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ProblemRecordRepository extends JpaRepository<ProblemRecord, Long> {

    List<ProblemRecord> findByUser(User user);

    List<ProblemRecord> findBySolvedDate(LocalDate solvedDate);

    // Delete all problem records by a user
    void deleteByUser(User user);
}
