package com.college.campuscollab.entity;

public enum AccountStatus {
    ACTIVE, // Normal - can do everything
    SUSPENDED, // Can login but read-only - cannot upload/edit
    INACTIVE // Completely disabled - cannot login
}
