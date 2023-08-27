package com.github.tornado2023team5.kanjichan.service;

import com.github.tornado2023team5.kanjichan.entity.Action;
import com.github.tornado2023team5.kanjichan.entity.Asobi;
import com.github.tornado2023team5.kanjichan.entity.LineId;
import com.github.tornado2023team5.kanjichan.entity.User;
import com.github.tornado2023team5.kanjichan.model.AsobiPlanningSession;
import com.github.tornado2023team5.kanjichan.model.function.ShopCategory;
import com.github.tornado2023team5.kanjichan.util.RestfulAPIUtil;
import com.google.maps.errors.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SetupScheduleService {
    private final RestfulAPIUtil restfulAPIUtil;
    private final RestTemplate restTemplate;
    //    private static final String BASE_URL = "http://db_server:4000";
    private static final String BASE_URL = "https://moon-usa.jp";
    private final static Random random = new Random();
    public static final HashMap<String, AsobiPlanningSession> sessions = new HashMap<>();
    private final GoogleMapsService googleMapsService;

    public void start(String id, String lineId) {
        var session = new AsobiPlanningSession();
        Asobi asobi = restTemplate.getForObject(BASE_URL + "/api/asobi/start", Asobi.class);
        session.setId(asobi.getId());
        session.setUsers(new ArrayList<>());
        sessions.put(id, session);
        addUser(id, lineId);
    }

    public void reset(String id) {
        sessions.remove(id);
    }

    public void addUser(String id, String lineId) {
        var user = restTemplate.getForObject(BASE_URL + "/api/lineId/" + lineId, String.class);
        sessions.get(id).getUsers().add(user);
    }

    public void confirm(String id) throws ParseException {
        var session = sessions.get(id);
        var asobi = new Asobi();
        asobi.setId(session.getId());
        asobi.setActions(session.getActions());
        asobi.setParticipantIds(session.getUsers());
        var actions = asobi.getActions();

        var baseTime = "2023-08-25T15:00:00.000Z";
        var sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = sdf.parse(baseTime);
        long threeHoursInMillis = 3 * 60 * 60 * 1000;

        for (Action action : actions) {
            date.setTime(date.getTime() + threeHoursInMillis);
            action.setStart(sdf.format(date));
            date.setTime(date.getTime() + threeHoursInMillis);
            action.setEnd(sdf.format(date));
        }
        restTemplate.postForObject(BASE_URL + "/api/asobi",asobi, Asobi.class);
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
        for (int i = 0; i < 5; i++) {
            var draft = new ArrayList<Action>();
            // 各採用スポットからランダムに1つづつ選ぶ
            for (var actions : session.getResultsList()) {
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
