package com.github.tornado2023team5.kanjichan.model;

import com.github.tornado2023team5.kanjichan.entity.Action;
import com.github.tornado2023team5.kanjichan.entity.Asobi;
import com.github.tornado2023team5.kanjichan.entity.User;
import com.github.tornado2023team5.kanjichan.service.SetupScheduleService;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResult;
import lombok.Data;

import java.util.*;

@Data
public class AsobiPlanningSession {
    String id;
    List<Asobi> asobis;
    List<Action> actions;
    String location;
    List<String> users;
    List<PlacesSearchResult> results;
    List<List<PlaceDetails>> resultsList = new ArrayList<>();
    List<List<Action>> drafts;

    public boolean isEditting() {
        return SetupScheduleService.sessions.values().stream().anyMatch(session -> session.getId().equals(id));
    }
}
