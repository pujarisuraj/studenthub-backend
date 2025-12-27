package com.college.campuscollab.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "problem_records")
@Getter
@Setter
public class ProblemRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private String platform;
    private String problemLink;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    private Integer points;
    private LocalDate solvedDate;

}
