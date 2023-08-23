package com.github.tornado2023team5.kanjichan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    private String id;
    private LineProfile line;
    private GoogleToken google;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
    private List<Schedule> schedules;
    private List<Asobi> paticipates;
    private List<Comment> comments;
}