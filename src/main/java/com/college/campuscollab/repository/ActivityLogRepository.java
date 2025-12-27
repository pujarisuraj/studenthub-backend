package com.college.campuscollab.repository;

import com.college.campuscollab.entity.ActivityLog;
import com.college.campuscollab.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

        // Find all logs ordered by timestamp descending (newest first)
        Page<ActivityLog> findAllByOrderByTimestampDesc(Pageable pageable);

        // Find logs by user
        Page<ActivityLog> findByUserOrderByTimestampDesc(User user, Pageable pageable);

        // Find logs by user email
        Page<ActivityLog> findByUserEmailOrderByTimestampDesc(String userEmail, Pageable pageable);

        // Find logs by action type
        Page<ActivityLog> findByActionTypeOrderByTimestampDesc(String actionType, Pageable pageable);

        // Find logs by action category
        Page<ActivityLog> findByActionCategoryOrderByTimestampDesc(String actionCategory, Pageable pageable);

        // Find logs by entity type and ID
        List<ActivityLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, Long entityId);

        // Find logs within a date range
        Page<ActivityLog> findByTimestampBetweenOrderByTimestampDesc(
                        LocalDateTime startDate,
                        LocalDateTime endDate,
                        Pageable pageable);

        // Find logs by user role
        Page<ActivityLog> findByUserRoleOrderByTimestampDesc(
                        ActivityLog.UserRole userRole,
                        Pageable pageable);

        // Find failed actions
        Page<ActivityLog> findByStatusOrderByTimestampDesc(String status, Pageable pageable);

        // Custom query to search logs
        @Query("SELECT a FROM ActivityLog a WHERE " +
                        "LOWER(a.userEmail) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(a.userFullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(a.actionType) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(a.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                        "ORDER BY a.timestamp DESC")
        Page<ActivityLog> searchLogs(@Param("searchTerm") String searchTerm, Pageable pageable);

        // Get recent activity count for dashboard
        @Query("SELECT COUNT(a) FROM ActivityLog a WHERE a.timestamp >= :since")
        Long countRecentActivity(@Param("since") LocalDateTime since);

        // Get activity stats by category
        @Query("SELECT a.actionCategory, COUNT(a) FROM ActivityLog a " +
                        "WHERE a.timestamp >= :since " +
                        "GROUP BY a.actionCategory")
        List<Object[]> getActivityStatsByCategory(@Param("since") LocalDateTime since);

        // Get most active users
        @Query("SELECT a.userEmail, a.userFullName, COUNT(a) as activityCount FROM ActivityLog a " +
                        "WHERE a.timestamp >= :since " +
                        "GROUP BY a.userEmail, a.userFullName " +
                        "ORDER BY activityCount DESC")
        List<Object[]> getMostActiveUsers(@Param("since") LocalDateTime since, Pageable pageable);

        // Delete all activity logs for a user
        void deleteByUser(User user);
}
