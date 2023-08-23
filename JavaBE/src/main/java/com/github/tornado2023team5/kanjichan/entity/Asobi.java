package com.github.tornado2023team5.kanjichan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Asobi {

    private String id;

    private String name;

    private String description;

    private List<User> participants;

    private List<Action> actions;
}