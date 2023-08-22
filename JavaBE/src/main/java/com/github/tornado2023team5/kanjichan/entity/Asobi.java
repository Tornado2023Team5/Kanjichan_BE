package com.github.tornado2023team5.kanjichan.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Asobi {

    @Id
    private String id;

    private String name;

    private String description;

    @ManyToMany
    @JoinTable(
            name = "asobi_user",
            joinColumns = @JoinColumn(name = "asobi_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> participants;

    @OneToMany(mappedBy = "asobi", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Action> actions;

    @OneToMany(mappedBy = "asobi", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "asobi_schedule",
            joinColumns = @JoinColumn(name = "asobi_id"),
            inverseJoinColumns = @JoinColumn(name = "schedule_id")
    )
    private List<Schedule> schedules;

    // getters, setters, etc.
}