package com.github.tornado2023team5.kanjichan.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class LineProfile {

    @Id
    private String id;

    private String lineUserId;  // この属性には@UniqueConstraintを使用して一意性を保証することができます。

    private String pictureUrl;

    private String lineUserName;

    @OneToOne(mappedBy = "line")
    private User user;

    // getters, setters, etc.
}