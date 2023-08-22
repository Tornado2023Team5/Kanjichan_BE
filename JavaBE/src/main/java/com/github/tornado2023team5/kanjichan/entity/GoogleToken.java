package com.github.tornado2023team5.kanjichan.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class GoogleToken {

    @Id
    private String id;

    private String token;

    @OneToOne(mappedBy = "googleToken")
    private User user;

    // getters, setters, etc.
}