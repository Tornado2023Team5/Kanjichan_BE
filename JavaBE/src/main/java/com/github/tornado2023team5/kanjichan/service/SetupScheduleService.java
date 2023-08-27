package com.github.tornado2023team5.kanjichan.service;

import com.github.tornado2023team5.kanjichan.entity.Action;
import com.github.tornado2023team5.kanjichan.entity.Asobi;
import com.github.tornado2023team5.kanjichan.entity.User;
import com.github.tornado2023team5.kanjichan.model.AsobiPlanningSession;
import com.github.tornado2023team5.kanjichan.model.function.ShopCategory;
import com.github.tornado2023team5.kanjichan.util.RestfulAPIUtil;
import com.google.maps.errors.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SetupScheduleService {
    private final RestfulAPIUtil restfulAPIUtil;
    private final static Random random = new Random();
    public static final HashMap<String , AsobiPlanningSession> sessions = new HashMap<>();
    private final GoogleMapsService googleMapsService;

    public void start(String id, List<String> lineIds) {
        var session = new AsobiPlanningSession();
        Asobi asobi = restfulAPIUtil.get("/api/asobi/start");
        List<User> users = restfulAPIUtil.post("/api/asobi/start", lineIds);
        session.setId(asobi.getId());
        session.setUsers(users);
        sessions.put(id, session);
    }

    public void debug(String id, List<String> lineIds) {
        var session = new AsobiPlanningSession();
        Asobi asobi = new Asobi();
        asobi.setId(id);
        List<User> users = Arrays.asList(new User(), new User());
        session.setId(asobi.getId());
        session.setUsers(users);
        sessions.put(id, session);
    }

    public void reset(String id) {
        sessions.remove(id);
    }

    public void addUser(String id, String lineId) {
        User user = restfulAPIUtil.get("/api/lineId?lineIds=" + lineId);
        sessions.get(id).getUsers().add(user);
    }

    public void confirm(String id) {
        var session = sessions.get(id);
        var asobi = new Asobi();
        asobi.setId(session.getId());
        asobi.setActions(session.getActions());
        asobi.setParticipants(session.getUsers());
        restfulAPIUtil.post("/api/asobi", asobi);
        sessions.remove(id);
    }

    public boolean isEditting(String id) {
        return sessions.containsKey(id);
    }

    public void setLocation(String id, String location) {
        sessions.get(id).setLocation(location);
    }

    public AsobiPlanningSession getSession(String id) {
        return sessions.get(id);
    }

    public void draft(String id) {
        List<List<Action>> drafts = new ArrayList<>();
        var session = getSession(id);
        // 草案を4つ作成する
        for (int i = 0 ; i < 5 ; i++) {
            var draft = new ArrayList<Action>();
            // 各採用スポットからランダムに1つづつ選ぶ
            for(var actions : session.getResultsList()) {
                var result = actions.get(random.nextInt(actions.size()));
                var action = new Action();
                action.setName(result.name);
                action.setLocation(result.formattedAddress);
                draft.add(action);
            }
            drafts.add(draft);
        }
        session.setDrafts(drafts);
    }

    public void decideDraft(AsobiPlanningSession session, List<Action> draft) throws IOException, InterruptedException, ApiException {
        googleMapsService.sortByDistance(draft, session.getLocation() + "駅");
        session.setActions(draft);
        session.setDrafts(null);
    }
}
