package com.github.tornado2023team5.kanjichan.service;

import com.github.tornado2023team5.kanjichan.entity.Action;
import com.github.tornado2023team5.kanjichan.entity.Asobi;
import com.github.tornado2023team5.kanjichan.entity.User;
import com.github.tornado2023team5.kanjichan.model.AsobiPlanningSession;
import com.github.tornado2023team5.kanjichan.util.RestfulAPIUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SetupScheduleService {
    private final RestfulAPIUtil restfulAPIUtil;
    private final static Random random = new Random();
    private static final HashMap<String , AsobiPlanningSession> sessions = new HashMap<>();

    public void start(String id, List<String> lineIds) {
        var session = new AsobiPlanningSession();
        Asobi asobi = restfulAPIUtil.get("http://localhost:4000/api/asobi/start");
        List<User> users = restfulAPIUtil.post("http://localhost:4000/api/asobi/start", lineIds);
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

    public boolean isStarted(String id) {
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
        for (int i = 0 ; i < 3 ; i++) {
            var draft = new ArrayList<Action>();
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
}
