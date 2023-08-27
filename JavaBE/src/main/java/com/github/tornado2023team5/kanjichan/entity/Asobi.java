package com.github.tornado2023team5.kanjichan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class Asobi {

    private String id;

    private String name;

    private String description = "";

    private List<String> participantIds = new ArrayList<>();

    private List<Action> actions = new ArrayList<>();
}