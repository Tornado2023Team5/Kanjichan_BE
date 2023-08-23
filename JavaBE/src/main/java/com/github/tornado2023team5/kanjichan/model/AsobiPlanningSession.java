package com.github.tornado2023team5.kanjichan.model;

import com.github.tornado2023team5.kanjichan.entity.Action;
import com.github.tornado2023team5.kanjichan.entity.Asobi;
import com.github.tornado2023team5.kanjichan.entity.User;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Data
public class AsobiPlanningSession {
    String id;
    List<Asobi> asobis;
    List<Action> actions;
    String location;
    List<User> users;
}
