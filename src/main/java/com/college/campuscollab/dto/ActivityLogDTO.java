package com.college.campuscollab.dto;

import com.college.campuscollab.entity.ActivityLog;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogDTO {

    private Long id;
    private String userEmail;
    private String userFullName;
    private String userRole;
    private String actionType;
    private String actionCategory;
    private String description;
    private String entityType;
    private Long entityId;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private String status;
    private String errorMessage;
    private String timestamp; // Formatted as string for frontend
    private String timeAgo; // Human-readable time ago

    // Convert Entity to DTO
    public static ActivityLogDTO fromEntity(ActivityLog log) {
        ActivityLogDTO dto = new ActivityLogDTO();
        dto.setId(log.getId());
        dto.setUserEmail(log.getUserEmail());
        dto.setUserFullName(log.getUserFullName());
        dto.setUserRole(log.getUserRole() != null ? log.getUserRole().name() : null);
        dto.setActionType(log.getActionType());
        dto.setActionCategory(log.getActionCategory());
        dto.setDescription(log.getDescription());
        dto.setEntityType(log.getEntityType());
        dto.setEntityId(log.getEntityId());
        dto.setOldValue(log.getOldValue());
        dto.setNewValue(log.getNewValue());
        dto.setIpAddress(log.getIpAddress());
        dto.setStatus(log.getStatus());
        dto.setErrorMessage(log.getErrorMessage());

        // Format timestamp
        if (log.getTimestamp() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            dto.setTimestamp(log.getTimestamp().format(formatter));
            dto.setTimeAgo(calculateTimeAgo(log.getTimestamp()));
        }

        return dto;
    }

    // Calculate human-readable time ago
    private static String calculateTimeAgo(LocalDateTime timestamp) {
        LocalDateTime now = LocalDateTime.now();
        long seconds = java.time.Duration.between(timestamp, now).getSeconds();

        if (seconds < 60) {
            return seconds + " seconds ago";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (seconds < 604800) {
            long days = seconds / 86400;
            return days + (days == 1 ? " day ago" : " days ago");
        } else if (seconds < 2592000) {
            long weeks = seconds / 604800;
            return weeks + (weeks == 1 ? " week ago" : " weeks ago");
        } else if (seconds < 31536000) {
            long months = seconds / 2592000;
            return months + (months == 1 ? " month ago" : " months ago");
        } else {
            long years = seconds / 31536000;
            return years + (years == 1 ? " year ago" : " years ago");
        }
    }
}
