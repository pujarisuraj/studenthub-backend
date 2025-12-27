package com.college.campuscollab.repository;

import com.college.campuscollab.entity.Project;
import com.college.campuscollab.entity.ProjectLike;
import com.college.campuscollab.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectLikeRepository extends JpaRepository<ProjectLike, Long> {

    // Check if a user has liked a project
    boolean existsByProjectAndUser(Project project, User user);

    // Find a specific like by project and user
    Optional<ProjectLike> findByProjectAndUser(Project project, User user);

    // Count total likes for a project
    long countByProject(Project project);

    // Delete all likes by a user
    void deleteByUser(User user);

    // Delete all likes for a project
    void deleteByProject(Project project);
}
