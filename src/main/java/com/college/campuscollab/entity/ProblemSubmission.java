package com.college.campuscollab.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class ProblemSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String platform;      // LeetCode, CodeChef, etc.
    private String problemLink;
    private String difficulty;    // EASY, MEDIUM, HARD
    private int points;

    private LocalDate submissionDate;

    @ManyToOne
    private User user;
}
