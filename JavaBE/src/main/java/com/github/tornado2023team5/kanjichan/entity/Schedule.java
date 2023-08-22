package com.github.tornado2023team5.kanjichan.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Schedule {

    @Id
    private String id;

    private LocalDateTime date;

    private Boolean morning;

    private Boolean afternoon;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    @ManyToMany(mappedBy = "schedules")
    private List<Asobi> asobis;

    // getters, setters, etc.
}