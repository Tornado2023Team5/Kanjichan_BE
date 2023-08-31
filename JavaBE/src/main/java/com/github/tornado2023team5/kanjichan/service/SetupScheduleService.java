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
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.message.TextMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    private final LineMessagingClient lineMessagingClient;
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

    public List<String> getGoogleCalendarUsers(String groupId) {
        return Arrays.asList(restTemplate.getForObject(BASE_URL + "/api/line/group/google/" + groupId, String[].class));
    }


    public LocalDateTime confirm(String id) {
        var asobi = new Asobi(getSession(id));

        var actions = asobi.getActions();
        List<LocalDateTime> freeTimes = findCommonFreeTimes(asobi.getParticipantIds(), LocalDateTime.now());
        if (freeTimes.size() == 0) return null;

        var date = freeTimes.get(0);

        for (int i = 1; i < actions.size(); i++) {
            Action action = actions.get(i);
            var diff = (i - 1) * 3L;
            action.setStart(date.plusHours(diff).format(formatter));
            action.setEnd(date.plusHours(diff + 3L).format(formatter));
        }
        actions.get(0).setStart(date.format(formatter));
        actions.get(0).setEnd(date.format(formatter));
        restTemplate.postForObject(BASE_URL + "/api/asobi", asobi, Asobi.class);
        var googleUsers = getGoogleCalendarUsers(id);
        var message = createEventUrl(asobi.getName(), asobi.getDescription(), actions.get(0).getLocation(), actions.get(0).getStart(), actions.get(actions.size() - 1).getEnd());
        googleUsers.forEach(userId -> {
            var textMessage = new TextMessage(message);
            var pushMessage = new PushMessage(userId, textMessage);
            lineMessagingClient.pushMessage(pushMessage);
        });
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
        draft = googleMapsService.sortByDistance(draft, session.getLocation() + "駅");
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
                .sorted()
                .toList();
    }

    private List<LocalDateTime> extractFreeSlots(LocalDateTime date, List<Schedule> schedules) {
        List<LocalDateTime> freeSlots = new ArrayList<>();

        if (schedules.stream().allMatch(Schedule::getMorning))
            freeSlots.add(date.withHour(9));

        if (schedules.stream().allMatch(Schedule::getAfternoon))
            freeSlots.add(date.withHour(13));

        return freeSlots;
    }

    public static String createEventUrl(String eventName, String details, String location, LocalDateTime start, LocalDateTime end) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

        // Convert LocalDateTime to the required format
        String formattedStart = start.format(formatter);
        String formattedEnd = end.format(formatter);

        // Construct the URL
        String url = "https://www.google.com/calendar/render?action=TEMPLATE";

        url += "&text=" + URLEncoder.encode(eventName, StandardCharsets.UTF_8);
        url += "&dates=" + formattedStart + "/" + formattedEnd;
        url += "&details=" + URLEncoder.encode(details, StandardCharsets.UTF_8);
        url += "&location=" + URLEncoder.encode(location, StandardCharsets.UTF_8);

        return url;
    }

    public static String createEventUrl(String eventName, String details, String location, String start, String end) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
        // Construct the URL
        String url = "https://www.google.com/calendar/render?action=TEMPLATE";

        url += "&text=" + URLEncoder.encode(eventName, StandardCharsets.UTF_8);
        url += "&dates=" + start + "/" + end;
        url += "&details=" + URLEncoder.encode(details, StandardCharsets.UTF_8);
        url += "&location=" + URLEncoder.encode(location, StandardCharsets.UTF_8);

        return url;
    }
}
