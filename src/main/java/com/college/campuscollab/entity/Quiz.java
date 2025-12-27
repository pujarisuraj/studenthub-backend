package com.college.campuscollab.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String subject;        // Java, DBMS, OS
    private LocalDate createdDate;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL , fetch = FetchType.EAGER )
    private List<QuizQuestion> questions;


}
