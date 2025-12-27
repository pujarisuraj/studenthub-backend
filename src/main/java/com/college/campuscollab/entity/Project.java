package com.college.campuscollab.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "projects")
@Getter
@Setter
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String projectName;

    private String teamLeaderName;

    private String course;

    private Integer semester;

    private String techStack;

    @Column(length = 1000)
    private String description;

    // UPDATED LINKS
    private String liveLink; // hosted project output
    private String codeLink; // GitHub / source code
    private String status;

    // Screenshots (store file paths or URLs)
    @ElementCollection
    private List<String> screenshots;

    @ManyToOne
    private User owner;

    // Engagement metrics (nullable for existing data compatibility)
    private Integer viewCount = 0;

    private Integer likeCount = 0;

    // Timestamp (nullable for existing data compatibility)
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (viewCount == null) {
            viewCount = 0;
        }
        if (likeCount == null) {
            likeCount = 0;
        }
    }
}
