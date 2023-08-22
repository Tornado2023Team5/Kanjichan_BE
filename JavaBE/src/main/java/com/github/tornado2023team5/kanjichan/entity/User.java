package com.github.tornado2023team5.kanjichan.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class User {

    @Id
    private String id;

    @OneToOne
    @JoinColumn(name = "lineId", unique = true)
    private LineProfile line;

    @OneToOne
    @JoinColumn(name = "googleTokenId", unique = true)
    private GoogleToken google;

    private LocalDateTime createdat;

    private LocalDateTime deletedat;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Schedule> schedules;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Asobi> paticipates;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    // getters, setters, etc.
}