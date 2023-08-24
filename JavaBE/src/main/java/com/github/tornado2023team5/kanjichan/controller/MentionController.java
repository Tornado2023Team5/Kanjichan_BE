package com.github.tornado2023team5.kanjichan.controller;

import com.github.tornado2023team5.kanjichan.entity.Action;
import com.github.tornado2023team5.kanjichan.service.GoogleMapsService;
import com.github.tornado2023team5.kanjichan.service.LineMessageService;
import com.github.tornado2023team5.kanjichan.service.PickUpCategoryService;
import com.github.tornado2023team5.kanjichan.service.SetupScheduleService;
import com.google.maps.errors.ApiException;
import com.google.maps.model.PlacesSearchResult;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@LineMessageHandler
@RestController
@RequiredArgsConstructor
public class MentionController {
    private final LineMessagingClient lineMessagingClient;
    private final SetupScheduleService setupScheduleService;
    private final PickUpCategoryService service;
    private final GoogleMapsService googleMapsService;

    @EventMapping
    public TextMessage handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws ExecutionException, InterruptedException, IOException, ApiException {
        Source source = event.getSource();
        var messageText = event.getMessage().getText();
        if (!messageText.contains("@Moon")) return null;
        String[] lines = messageText.split("\n");
        String contentText = Arrays.stream(lines).skip(1).collect(Collectors.joining("\n"));
        String[] args = Arrays.stream(lines[0].split(" ")).skip(1).toArray(String[]::new);

        if (source instanceof GroupSource groupSource) {

            String id = groupSource.getGroupId();
            // メンションに反応する処理
            switch (args[0]) {
                case "予定" -> {
                    if (setupScheduleService.isStarted(id)) {
                        send(id, "既に予定を立てています。");
                        return null;
                    }
                    setupScheduleService.debug(id, new ArrayList<>(Collections.singletonList(source.getSenderId())));
                    send(id, "予定を立てました。");
                    send(id, "活動場所を教えてください。@bot 場所 [場所]");
                    send(id, "例: @bot 場所 渋谷");
                }
                case "場所" -> {
                    if (!setupScheduleService.isStarted(id)) {
                        send(id, "予定を立てていません。");
                        return null;
                    }
                    send(id, "場所を設定しました。");
                    setupScheduleService.setLocation(id, args[1]);
                }
                case "調査" -> {
                    if (!setupScheduleService.isStarted(id)) {
                        send(id, "予定を立てていません。");
                        return null;
                    }
                    var session = setupScheduleService.getSession(id);
                    if (session.getLocation() == null) {
                        send(id, "場所を設定していません。");
                        return null;
                    }
                    var category = service.pickUp(args[1]);
                    send(id, "「" + session.getLocation() + "」周辺の" + "「" + category + "」" + "を調査します。");
                    session.setResults(Arrays.asList(googleMapsService.getShopInfo(session.getLocation(), category)));
                    send(id, session.getResults().stream().map(place -> place.name).collect(Collectors.joining("\n")));
                }
                case "削除" -> {
                    if (!setupScheduleService.isStarted(id)) {
                        send(id, "予定を立てていません。");
                        return null;
                    }
                    var session = setupScheduleService.getSession(id);
                    if (session.getResults() == null) {
                        send(id, "調査結果がありません。");
                        return null;
                    }
                    int index;
                    try {
                        index = Integer.parseInt(args[1]);
                    } catch (NumberFormatException exception) {
                        send(id, "数字を入力してください。");
                        return null;
                    }
                    if (index < 0 || index >= session.getResults().size()) {
                        send(id, "数字が範囲外です。");
                        return null;
                    }
                    session.getResults().remove(index);
                    send(id, "削除しました。");
                    send(id, session.getResults().stream().map(place -> place.name).collect(Collectors.joining("\n")));
                }
                case "採用" -> {
                    if (!setupScheduleService.isStarted(id)) {
                        send(id, "予定を立てていません。");
                        return null;
                    }
                    var session = setupScheduleService.getSession(id);
                    if (session.getResults() == null) {
                        send(id, "調査結果がありません。");
                        return null;
                    }
                    session.adopt();
                    send(id, "調査結果を採用しました。");
                }
                case "追加" -> {
                    if (!setupScheduleService.isStarted(id)) {
                        send(id, "予定を立てていません。");
                        return null;
                    }
                    var session = setupScheduleService.getSession(id);
                    session.getActions();
                }
                case "草案" -> {
                    if (!setupScheduleService.isStarted(id)) {
                        send(id, "予定を立てていません。");
                        return null;
                    }
                    var session = setupScheduleService.getSession(id);
                    if (session.getActions() == null) {
                        send(id, "調査結果がありません。");
                        return null;
                    }
                    send(id, """
                            草案を作成しました。
                            採用する案を決めてください
                            """);

                    setupScheduleService.draft(id);
                    for (int i = 0; i < session.getDrafts().size(); i++) {
                        send(id, "草案" + i + "\n" + session.getDrafts().get(i).stream().map(Action::getName).collect(Collectors.joining("\n")));
                    }
                }
                case "確定" -> {
                    if (!setupScheduleService.isStarted(id)) {
                        send(id, "予定を立てていません。");
                        return null;
                    }
                    var session = setupScheduleService.getSession(id);
                    if (session.getDrafts() == null) {
                        send(id, "草案がありません。");
                        return null;
                    }
                    int index;
                    try {
                        index = Integer.parseInt(args[1]);
                    } catch (NumberFormatException exception) {
                        send(id, "数字を入力してください。");
                        return null;
                    }
                    if (index < 0 || index >= session.getDrafts().size()) {
                        send(id, "数字が範囲外です。");
                        return null;
                    }
                    session.setActions(session.getDrafts().get(index));
                    send(id, "確定しました。");
                }
                case "編集" -> {

                }
                default -> {
                    send(id, """
                            使い方:
                            @bot 予定
                            @bot 確定
                            @bot 編集
                            @bot 調査
                            @bot 追加
                            """);
                }
            }
        }
        return null;
    }

    private void send(String groupId, String text) {
        var textMessage = TextMessage.builder().text(text).build();
        var pushMessage = new PushMessage(groupId, textMessage);
        lineMessagingClient.pushMessage(pushMessage);
    }
}

