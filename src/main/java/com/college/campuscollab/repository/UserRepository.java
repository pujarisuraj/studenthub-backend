package com.college.campuscollab.repository;

import com.college.campuscollab.entity.Role;
import com.college.campuscollab.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRollNumber(String rollNumber);

    // Find users by role
    List<User> findByRole(Role role);

    // Find users by role and course
    List<User> findByRoleAndCourse(Role role, String course);

    // Count users by role
    long countByRole(Role role);
}
