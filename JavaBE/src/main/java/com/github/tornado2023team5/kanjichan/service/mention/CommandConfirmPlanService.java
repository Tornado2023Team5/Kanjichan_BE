package com.github.tornado2023team5.kanjichan.service.mention;

import com.github.tornado2023team5.kanjichan.entity.Action;
import com.github.tornado2023team5.kanjichan.service.GoogleMapsService;
import com.github.tornado2023team5.kanjichan.service.SetupScheduleService;
import com.google.maps.errors.ApiException;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.message.TextMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CommandConfirmPlanService {
    private final SetupScheduleService setupScheduleService;
    private final GoogleMapsService googleMapsService;
    private final LineMessagingClient lineMessagingClient;

    public void confirmPlan(String id, StringBuilder reply) throws IOException, InterruptedException, ApiException {
        var session = setupScheduleService.getSession(id);
        setupScheduleService.draft(id);
        reply.append(session.getDrafts().get(0).stream().map(Action::getName).collect(Collectors.joining("\n↓\n")));
        setupScheduleService.decideDraft(session, session.getDrafts().get(0));
        session.setUsers(setupScheduleService.getUsers(id).getUserIds());
        LocalDateTime date = setupScheduleService.confirm(id);

        reply.append("予定内容:\n");
        reply.append("◦ 日程: ").append(date).append("\n");
        reply.append("◦ 場所: ").append(googleMapsService.getStation(session.getLocation()).name).append("\n\n");
        reply.append("楽しんできてほしいウサ！\uD83D\uDC30✨");

        var directMessage = new StringBuilder();
        directMessage.append("\uD83D\uDE80\uD83E\uDD55遊びの予定が決まったうさ\uD83E\uDD55\uD83D\uDE80\n");
        directMessage.append("◦ グループ: ").append(session.getName()).append("\n");
        directMessage.append("◦ 日程: ").append(date).append("\n");
        directMessage.append("◦ 場所: ").append(googleMapsService.getStation(session.getLocation()).name).append("\n\n");
        directMessage.append("楽しんできてほしいウサ！\uD83D\uDC30✨");

        var lineUsers = setupScheduleService.getLineUsers(id);
        var googleUsers = setupScheduleService.getGoogleCalendarUsers(id);
        var addCalendarText = setupScheduleService.createEventUrl(session.getName(), "", session.getActions().get(0).getLocation(), session.getActions().get(0).getStart(), session.getActions().get(session.getActions().size() - 1).getEnd());

        lineUsers.getLineUserIds().forEach(userId -> {
            var textMessage = new TextMessage(directMessage.toString());
            var addCalendarMessage = new TextMessage("Googleカレンダーに追加するリンクウサ！忘れないよにするウサ\uD83D\uDCC5" + "\n" + addCalendarText);
            var pushMessage = googleUsers.contains(userId) ? new PushMessage(userId, Arrays.asList(textMessage, addCalendarMessage)) : new PushMessage(userId, textMessage);
            lineMessagingClient.pushMessage(pushMessage);
        });
    }

}
