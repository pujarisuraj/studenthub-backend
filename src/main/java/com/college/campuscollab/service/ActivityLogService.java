package com.college.campuscollab.service;

import com.college.campuscollab.dto.ActivityLogDTO;
import com.college.campuscollab.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ActivityLogService {

        // Log basic activity
        void logActivity(User user, String actionType, String actionCategory, String description);

        // Log entity-related activity
        void logEntityActivity(User user, String actionType, String actionCategory,
                        String description, String entityType, Long entityId);

        // Log update activity with old and new values
        void logUpdateActivity(User user, String actionType, String actionCategory,
                        String description, String entityType, Long entityId,
                        String oldValue, String newValue);

        // Log activity with IP and User-Agent
        void logActivityWithContext(User user, String actionType, String actionCategory,
                        String description, String ipAddress, String userAgent);

        // Get all activity logs (paginated)
        Page<ActivityLogDTO> getAllLogs(Pageable pageable);

        // Get logs by user
        Page<ActivityLogDTO> getLogsByUser(String userEmail, Pageable pageable);

        // Get logs by action category
        Page<ActivityLogDTO> getLogsByCategory(String category, Pageable pageable);

        // Get logs by date range
        Page<ActivityLogDTO> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

        // Search logs
        Page<ActivityLogDTO> searchLogs(String searchTerm, Pageable pageable);

        // Get activity statistics
        Map<String, Object> getActivityStatistics(LocalDateTime since);

        // Get recent activity count
        Long getRecentActivityCount(LocalDateTime since);

        // Get most active users
        List<Map<String, Object>> getMostActiveUsers(int limit);

        // Clear all activity logs
        long clearAllLogs();
}
