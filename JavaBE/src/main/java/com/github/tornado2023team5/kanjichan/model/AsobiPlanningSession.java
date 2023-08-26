package com.github.tornado2023team5.kanjichan.model;

import com.github.tornado2023team5.kanjichan.entity.Action;
import com.github.tornado2023team5.kanjichan.entity.Asobi;
import com.github.tornado2023team5.kanjichan.entity.User;
import com.google.maps.model.PlacesSearchResult;
import lombok.Data;

import java.util.*;

@Data
public class AsobiPlanningSession {
    String id;
    List<Asobi> asobis;
    List<Action> actions;
    String location;
    List<User> users;
    List<PlacesSearchResult> results;
    List<List<PlacesSearchResult>> resultsList = new ArrayList<>();
    List<List<Action>> drafts;

    public void adopt() {
        resultsList.add(results);
        results = null;
    }
}