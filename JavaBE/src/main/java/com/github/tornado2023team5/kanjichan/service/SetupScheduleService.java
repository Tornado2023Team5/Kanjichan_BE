package com.github.tornado2023team5.kanjichan.service;

import com.github.tornado2023team5.kanjichan.entity.*;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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

    public void confirm(String id) {
        var session = sessions.get(id);
        var asobi = new Asobi();
        asobi.setId(session.getId());
        asobi.setActions(session.getActions());
        asobi.setParticipantIds(session.getUsers());
        var actions = asobi.getActions();


        List<LocalDateTime> freeTimes = findCommonFreeTimes(session.getUsers(), new Date());
        if(freeTimes.size() == 0) return;

        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        var date = freeTimes.get(0);


        for (int i = 0; i < actions.size(); i++) {
            Action action = actions.get(i);
            action.setStart(date.plusHours(3L * i).format(formatter));
            action.setEnd(date.plusHours(3L * i + 3).format(formatter));
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

    public List<LocalDateTime> findCommonFreeTimes(List<String> userIds, Date baseDate) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, List<Schedule>> userSchedulesMap = new HashMap<>();

        // Calculate end date (baseDate + 14 days)
        LocalDateTime endDateTime = baseDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().plusDays(14);
        Date endDate = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());

        for (String userId : userIds) {
            Schedule[] schedules = restTemplate.getForObject("http://localhost:4000/api/schedule/?lineUserId={userId}&start={startDate}&end={endDate}", Schedule[].class, userId, baseDate, endDate);
            userSchedulesMap.put(userId, List.of(schedules));
        }

        return userSchedulesMap.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(Schedule::getDate, Collectors.toList()))
                .entrySet().stream()
                .flatMap(entry -> extractFreeSlots(entry.getKey(), entry.getValue()).stream())
                .collect(Collectors.toList());
    }

    private List<LocalDateTime> extractFreeSlots(LocalDateTime date, List<Schedule> schedules) {
        List<LocalDateTime> freeSlots = new ArrayList<>();

        if (isFreeMorningForAll(schedules)) {
            freeSlots.add(date.withHour(9).withMinute(0));
        }

        if (isFreeAfternoonForAll(schedules)) {
            freeSlots.add(date.withHour(13).withMinute(0));
        }

        return freeSlots;
    }

    private boolean isFreeMorningForAll(List<Schedule> schedules) {
        return schedules.stream().allMatch(Schedule::getMorning);
    }

    private boolean isFreeAfternoonForAll(List<Schedule> schedules) {
        return schedules.stream().allMatch(Schedule::getAfternoon);
    }
}
