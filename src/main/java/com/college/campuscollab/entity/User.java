package com.college.campuscollab.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String rollNumber;

    private String course;

    private Integer semester;

    private String password;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Role role; // STUDENT, SENIOR, VOLUNTEER, ADMIN

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AccountStatus accountStatus = AccountStatus.ACTIVE; // Default ACTIVE

    private LocalDateTime lastLoginAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (accountStatus == null) {
            accountStatus = AccountStatus.ACTIVE;
        }
    }
}
