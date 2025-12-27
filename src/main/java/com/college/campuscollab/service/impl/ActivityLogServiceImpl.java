package com.college.campuscollab.service.impl;

import com.college.campuscollab.dto.ActivityLogDTO;
import com.college.campuscollab.entity.ActivityLog;
import com.college.campuscollab.entity.User;
import com.college.campuscollab.repository.ActivityLogRepository;
import com.college.campuscollab.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Override
    @Async
    @Transactional
    public void logActivity(User user, String actionType, String actionCategory, String description) {
        try {
            ActivityLog activityLog = ActivityLog.create(user, actionType, actionCategory, description);
            activityLogRepository.save(activityLog);
            log.info("Activity logged for user {}: {} - {}", user.getEmail(), actionType, description);
        } catch (Exception e) {
            log.error("Failed to log activity for user {}: {}", user.getEmail(), e.getMessage());
        }
    }

    @Override
    @Async
    @Transactional
    public void logEntityActivity(User user, String actionType, String actionCategory,
            String description, String entityType, Long entityId) {
        try {
            ActivityLog activityLog = ActivityLog.createForEntity(user, actionType, actionCategory,
                    description, entityType, entityId);
            activityLogRepository.save(activityLog);
            log.info("Entity activity logged for user {}: {} on {} {}",
                    user.getEmail(), actionType, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to log entity activity for user {}: {}", user.getEmail(), e.getMessage());
        }
    }

    @Override
    @Async
    @Transactional
    public void logUpdateActivity(User user, String actionType, String actionCategory,
            String description, String entityType, Long entityId,
            String oldValue, String newValue) {
        try {
            ActivityLog activityLog = ActivityLog.createForUpdate(user, actionType, actionCategory,
                    description, entityType, entityId, oldValue, newValue);
            activityLogRepository.save(activityLog);
            log.info("Update activity logged for user {}: {} on {} {}",
                    user.getEmail(), actionType, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to log update activity for user {}: {}", user.getEmail(), e.getMessage());
        }
    }

    @Override
    @Async
    @Transactional
    public void logActivityWithContext(User user, String actionType, String actionCategory,
            String description, String ipAddress, String userAgent) {
        try {
            ActivityLog activityLog = ActivityLog.create(user, actionType, actionCategory, description);
            activityLog.setIpAddress(ipAddress);
            activityLog.setUserAgent(userAgent);
            activityLogRepository.save(activityLog);
            log.info("Activity with context logged for user {}: {}", user.getEmail(), actionType);
        } catch (Exception e) {
            log.error("Failed to log activity with context for user {}: {}", user.getEmail(), e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogDTO> getAllLogs(Pageable pageable) {
        Page<ActivityLog> logs = activityLogRepository.findAllByOrderByTimestampDesc(pageable);
        return logs.map(ActivityLogDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogDTO> getLogsByUser(String userEmail, Pageable pageable) {
        Page<ActivityLog> logs = activityLogRepository.findByUserEmailOrderByTimestampDesc(userEmail, pageable);
        return logs.map(ActivityLogDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogDTO> getLogsByCategory(String category, Pageable pageable) {
        Page<ActivityLog> logs = activityLogRepository.findByActionCategoryOrderByTimestampDesc(category, pageable);
        return logs.map(ActivityLogDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogDTO> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<ActivityLog> logs = activityLogRepository.findByTimestampBetweenOrderByTimestampDesc(
                startDate, endDate, pageable);
        return logs.map(ActivityLogDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogDTO> searchLogs(String searchTerm, Pageable pageable) {
        Page<ActivityLog> logs = activityLogRepository.searchLogs(searchTerm, pageable);
        return logs.map(ActivityLogDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getActivityStatistics(LocalDateTime since) {
        Map<String, Object> stats = new HashMap<>();

        // Total activity count
        Long totalCount = activityLogRepository.countRecentActivity(since);
        stats.put("totalActivities", totalCount);

        // Activity by category
        List<Object[]> categoryStats = activityLogRepository.getActivityStatsByCategory(since);
        Map<String, Long> categoryMap = new HashMap<>();
        for (Object[] stat : categoryStats) {
            categoryMap.put((String) stat[0], (Long) stat[1]);
        }
        stats.put("byCategory", categoryMap);

        // Most active users (top 10)
        List<Object[]> activeUsers = activityLogRepository.getMostActiveUsers(
                since, PageRequest.of(0, 10));
        List<Map<String, Object>> userList = new ArrayList<>();
        for (Object[] user : activeUsers) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("email", user[0]);
            userMap.put("fullName", user[1]);
            userMap.put("activityCount", user[2]);
            userList.add(userMap);
        }
        stats.put("mostActiveUsers", userList);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getRecentActivityCount(LocalDateTime since) {
        return activityLogRepository.countRecentActivity(since);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMostActiveUsers(int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(30); // Last 30 days
        List<Object[]> activeUsers = activityLogRepository.getMostActiveUsers(
                since, PageRequest.of(0, limit));

        List<Map<String, Object>> userList = new ArrayList<>();
        for (Object[] user : activeUsers) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("email", user[0]);
            userMap.put("fullName", user[1]);
            userMap.put("activityCount", user[2]);
            userList.add(userMap);
        }

        return userList;
    }

    /**
     * Clear all activity logs from database
     * 
     * @return number of deleted records
     */
    @Override
    @Transactional
    public long clearAllLogs() {
        long count = activityLogRepository.count();
        activityLogRepository.deleteAll();
        log.info("All activity logs cleared. Total deleted: {}", count);
        return count;
    }
}
