package com.college.campuscollab.repository;

import com.college.campuscollab.entity.Project;
import com.college.campuscollab.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

        List<Project> findByOwner(User owner);

        List<Project> findByStatus(String status);

        // Find projects by status ordered by creation date (newest first)
        List<Project> findByStatusOrderByCreatedAtDesc(String status);

        // Find all projects ordered by creation date (newest first)
        List<Project> findAllByOrderByCreatedAtDesc();

        // Search projects by name, description, or tech stack
        @Query("SELECT p FROM Project p WHERE " +
                        "LOWER(p.projectName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(p.techStack) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
        List<Project> searchProjects(@Param("searchTerm") String searchTerm);

        // Find by course
        List<Project> findByCourseIgnoreCase(String course);

        // Find by tech stack containing (case-insensitive)
        @Query("SELECT p FROM Project p WHERE LOWER(p.techStack) LIKE LOWER(CONCAT('%', :tech, '%'))")
        List<Project> findByTechStackContaining(@Param("tech") String tech);

        // Combined search with filters
        @Query("SELECT p FROM Project p WHERE " +
                        "(:status IS NULL OR p.status = :status) AND " +
                        "(:course IS NULL OR LOWER(p.course) = LOWER(:course)) AND " +
                        "(:techStack IS NULL OR LOWER(p.techStack) LIKE LOWER(CONCAT('%', :techStack, '%'))) AND " +
                        "(:searchTerm IS NULL OR " +
                        "LOWER(p.projectName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(p.techStack) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
                        "ORDER BY p.createdAt DESC")
        List<Project> findProjectsWithFilters(
                        @Param("status") String status,
                        @Param("course") String course,
                        @Param("techStack") String techStack,
                        @Param("searchTerm") String searchTerm);
}
