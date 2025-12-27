package com.college.campuscollab.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity to track all activities across the platform
 * Captures who did what, when, and what changed
 */
@Entity
@Table(name = "activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "user_full_name")
    private String userFullName;

    @Column(name = "user_role")
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType; // LOGIN, LOGOUT, UPLOAD_PROJECT, UPDATE_PROFILE, etc.

    @Column(name = "action_category", length = 50)
    private String actionCategory; // AUTH, PROJECT, PROFILE, COLLABORATION, ADMIN

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // Detailed description of what happened

    @Column(name = "entity_type", length = 50)
    private String entityType; // User, Project, CollaborationRequest

    @Column(name = "entity_id")
    private Long entityId; // ID of the affected entity

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue; // Previous value (for updates)

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue; // New value (for updates)

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "status", length = 20)
    private String status; // SUCCESS, FAILED, PENDING

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage; // If action failed

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    // Enum for user roles
    public enum UserRole {
        STUDENT, ADMIN
    }

    // Helper method to create a log entry
    public static ActivityLog create(User user, String actionType, String actionCategory,
            String description) {
        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setUserEmail(user.getEmail());
        log.setUserFullName(user.getFullName());
        log.setUserRole(UserRole.valueOf(user.getRole().name()));
        log.setActionType(actionType);
        log.setActionCategory(actionCategory);
        log.setDescription(description);
        log.setStatus("SUCCESS");
        return log;
    }

    // Helper method for entity-related actions
    public static ActivityLog createForEntity(User user, String actionType, String actionCategory,
            String description, String entityType, Long entityId) {
        ActivityLog log = create(user, actionType, actionCategory, description);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        return log;
    }

    // Helper method for update actions
    public static ActivityLog createForUpdate(User user, String actionType, String actionCategory,
            String description, String entityType, Long entityId,
            String oldValue, String newValue) {
        ActivityLog log = createForEntity(user, actionType, actionCategory, description,
                entityType, entityId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        return log;
    }
}
