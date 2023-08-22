package com.github.tornado2023team5.kanjichan.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Comment {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "asobiId")
    private Asobi asobi;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    private String string; // "string" is a reserved word in many contexts, consider renaming this attribute to something more descriptive, like "content" or "text"

    // getters, setters, etc.
}