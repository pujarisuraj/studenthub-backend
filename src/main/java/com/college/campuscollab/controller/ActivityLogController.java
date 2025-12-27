package com.college.campuscollab.controller;

import com.college.campuscollab.dto.ActivityLogDTO;
import com.college.campuscollab.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activity-logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    /**
     * Get all activity logs (Admin only)
     * GET /api/activity-logs
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ActivityLogDTO>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ActivityLogDTO> logs = activityLogService.getAllLogs(pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get activity logs by user
     * GET /api/activity-logs/user/{email}
     */
    @GetMapping("/user/{email}")
    @PreAuthorize("hasRole('ADMIN') or #email == authentication.principal.username")
    public ResponseEntity<Page<ActivityLogDTO>> getLogsByUser(
            @PathVariable String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ActivityLogDTO> logs = activityLogService.getLogsByUser(email, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get activity logs by category
     * GET /api/activity-logs/category/{category}
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ActivityLogDTO>> getLogsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ActivityLogDTO> logs = activityLogService.getLogsByCategory(category, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get activity logs by date range
     * GET /api/activity-logs/date-range
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ActivityLogDTO>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ActivityLogDTO> logs = activityLogService.getLogsByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * Search activity logs
     * GET /api/activity-logs/search
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ActivityLogDTO>> searchLogs(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ActivityLogDTO> logs = activityLogService.searchLogs(query, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get activity statistics
     * GET /api/activity-logs/statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getActivityStatistics(
            @RequestParam(required = false) Integer days) {

        LocalDateTime since = days != null
                ? LocalDateTime.now().minusDays(days)
                : LocalDateTime.now().minusDays(30); // Default last 30 days

        Map<String, Object> stats = activityLogService.getActivityStatistics(since);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get recent activity count
     * GET /api/activity-logs/recent-count
     */
    @GetMapping("/recent-count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getRecentActivityCount(
            @RequestParam(required = false) Integer hours) {

        LocalDateTime since = hours != null
                ? LocalDateTime.now().minusHours(hours)
                : LocalDateTime.now().minusHours(24); // Default last 24 hours

        Long count = activityLogService.getRecentActivityCount(since);
        return ResponseEntity.ok(Map.of("count", count, "since", since.toString()));
    }

    /**
     * Get most active users
     * GET /api/activity-logs/most-active-users
     */
    @GetMapping("/most-active-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getMostActiveUsers(
            @RequestParam(defaultValue = "10") int limit) {

        List<Map<String, Object>> users = activityLogService.getMostActiveUsers(limit);
        return ResponseEntity.ok(users);
    }

    /**
     * Clear all activity logs (Admin only)
     * DELETE /api/activity-logs/clear-all
     */
    @DeleteMapping("/clear-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> clearAllActivityLogs() {
        try {
            long deletedCount = activityLogService.clearAllLogs();

            Map<String, Object> response = Map.of(
                    "message", "All activity logs cleared successfully",
                    "deletedCount", deletedCount);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = Map.of(
                    "error", "Failed to clear activity logs: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
