package com.github.tornado2023team5.kanjichan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Action {

    private String name;

    private String description;

    private LocalDateTime start;

    private LocalDateTime end;

    private String location;

    // getters, setters, etc.
}