package com.github.tornado2023team5.kanjichan.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class Action {

    @Id
    private String id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "asobiId")
    private Asobi asobi;

    private String description;

    private LocalDateTime start;

    private LocalDateTime end;

    private String location;

    // getters, setters, etc.
}