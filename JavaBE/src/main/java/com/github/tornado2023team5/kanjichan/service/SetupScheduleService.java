package com.github.tornado2023team5.kanjichan.service;

import com.github.tornado2023team5.kanjichan.entity.Action;
import com.github.tornado2023team5.kanjichan.entity.Asobi;
import com.github.tornado2023team5.kanjichan.entity.Schedule;
import com.github.tornado2023team5.kanjichan.model.AsobiPlanningSession;
import com.github.tornado2023team5.kanjichan.model.GroupLineUserObject;
import com.github.tornado2023team5.kanjichan.model.GroupUserObject;
import com.github.tornado2023team5.kanjichan.model.GroupUserRegistry;
import com.github.tornado2023team5.kanjichan.util.RestfulAPIUtil;
import com.google.maps.errors.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
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
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public void start(String id, String lineId, String name) {
        var session = new AsobiPlanningSession();
        Asobi asobi = restTemplate.getForObject(BASE_URL + "/api/asobi/start", Asobi.class);
        session.setId(asobi.getId());
        session.setUsers(new ArrayList<>());
        session.setName(name);
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

    public void registerUser(GroupUserRegistry registry) {
        restTemplate.postForObject(BASE_URL + "/api/line/group", registry, Void.class);
    }

    public GroupUserObject getUsers(String groupId) {
        return restTemplate.getForObject(BASE_URL + "/api/line/group/user/" + groupId, GroupUserObject.class);
    }

    public GroupLineUserObject getLineUsers(String groupId) {
        return restTemplate.getForObject(BASE_URL + "/api/line/group/line/" + groupId, GroupLineUserObject.class);
    }


    public LocalDateTime confirm(String id) {
        var asobi = new Asobi(getSession(id));

        var actions = asobi.getActions();
        List<LocalDateTime> freeTimes = findCommonFreeTimes(asobi.getParticipantIds(), LocalDateTime.now());
        freeTimes.forEach(System.out::println);
        if (freeTimes.size() == 0) return null;

        var date = freeTimes.get(0);

        for (int i = 0; i < actions.size(); i++) {
            Action action = actions.get(i);
            action.setStart(date.plusHours(3L * i).format(formatter));
            action.setEnd(date.plusHours(3L * i + 3).format(formatter));
        }
        restTemplate.postForObject(BASE_URL + "/api/asobi", asobi, Asobi.class);
        sessions.remove(id);
        return date;
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

    public List<LocalDateTime> findCommonFreeTimes(List<String> userIds, LocalDateTime baseDate) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, List<Schedule>> userSchedulesMap = new HashMap<>();

        // Calculate end date (baseDate + 14 days)
        LocalDateTime endDate = baseDate.plusDays(14);

        for (String userId : userIds) {
            Schedule[] schedules = restTemplate.getForObject(BASE_URL + "/api/schedule?userId={userId}&start={startDate}&end={endDate}", Schedule[].class, userId, baseDate.format(formatter), endDate.format(formatter));
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

        System.out.println(date + " morning: " + schedules.stream().allMatch(Schedule::getMorning) + " afternoon: " + schedules.stream().allMatch(Schedule::getAfternoon));

        if (schedules.stream().allMatch(Schedule::getMorning))
            freeSlots.add(date.withHour(9));

        if (schedules.stream().allMatch(Schedule::getAfternoon))
            freeSlots.add(date.withHour(13));

        return freeSlots;
    }
}
