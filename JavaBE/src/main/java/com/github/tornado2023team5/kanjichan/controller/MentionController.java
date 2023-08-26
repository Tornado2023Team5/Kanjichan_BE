package com.github.tornado2023team5.kanjichan.controller;

import com.github.tornado2023team5.kanjichan.entity.Action;
import com.github.tornado2023team5.kanjichan.model.function.CommandInformationFormat;
import com.github.tornado2023team5.kanjichan.model.function.ShopCategory;
import com.github.tornado2023team5.kanjichan.model.function.command.*;
import com.github.tornado2023team5.kanjichan.service.*;
import com.google.maps.errors.ApiException;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@LineMessageHandler
@RestController
@RequiredArgsConstructor
public class MentionController {
    private final LineMessagingClient lineMessagingClient;
    private final SetupScheduleService setupScheduleService;
    private final FunctionCallService functionCallService;
    private final GoogleMapsService googleMapsService;

    @EventMapping
    public TextMessage formatInput(MessageEvent<TextMessageContent> event) throws InterruptedException, IOException, ApiException {
        Source source = event.getSource();
        var messageText = event.getMessage().getText();
        if (!messageText.contains("@Moon")) return null;
        String[] lines = messageText.split("\n");
        String contentText = Arrays.stream(lines).skip(1).collect(Collectors.joining("\n"));
        String[] args = Arrays.stream(lines[0].split(" ")).skip(1).toArray(String[]::new);
        if (!(source instanceof GroupSource groupSource)) return null;

        String id = groupSource.getGroupId();
        CommandInformationFormat format = functionCallService.detect(messageText.replace("@Moon", ""));
        switch (format.getCommandType()) {
            case NONE -> {
                send(groupSource.getGroupId(), "入力内容を正しく認識できませんでした。");
                return null;
            }
            case MAKE_PLAN -> {
                var command = functionCallService.makePlan(messageText);
                if (command == null) {
                    send(groupSource.getGroupId(), "入力内容を正しく認識できませんでした。");
                    return null;
                }
                makePlan(id, command);
            }
            case SET_LOCATION -> {
                var command = functionCallService.setLocation(messageText);
                if (command == null) {
                    send(groupSource.getGroupId(), "入力内容を正しく認識できませんでした。");
                    return null;
                }
                setDestination(id, command.getDestination());
            }
            case SEARCH_SPOTS -> {
                var command = functionCallService.searchSpots(messageText);
                if (command == null) {
                    send(groupSource.getGroupId(), "入力内容を正しく認識できませんでした。");
                    return null;
                }
                searchSpots(id, command.getCategory());
            }
            case REMOVE_SPOT -> removeSpot(id, messageText);
            case ADOPT_SPOTS -> adopt(id);
            case MAKE_DRAFT -> draft(id);
            case DECIDE_DRAFT -> decideDraft(id, messageText);
            case EDIT_AND_ADD_SPOT_TO_DECIDED_DRAFT -> editAndAddSpotFromDecidedDraft(id, messageText);
            case EDIT_AND_REMOVE_SPOT_FROM_DECIDED_DRAFT -> editAndRemoveSpotFromDecidedDraft(id, messageText);
            case EDIT_AND_CHANGE_SPOT_FROM_DECIDED_DRAFT -> editAndChangeSpotFromDecidedDraft(id, messageText);

        }
        return null;
    }

    public void makePlan(String id, MakePlanCommand command) throws IOException, InterruptedException, ApiException {
        if (setupScheduleService.isStarted(id)) {
            send(id, "既に予定を立てています。");
            return;
        }
        setupScheduleService.debug(id, new ArrayList<>());
        send(id, "予定を立てました。");
        setDestination(id, command.getDestination());
        searchSpots(id, command.getCategory());
    }

    public void setDestination(String id, String destination) {
        var session = setupScheduleService.getSession(id);
        if(session == null) {
            send(id, "予定を立てていません。");
            return;
        }
        if(destination == null) {
            send(id, "活動場所を教えてください。\n 例: \n @bot \n 渋谷で遊びたい！");
            return;
        }
        setupScheduleService.setLocation(id, destination);
        send(id, "活動場所を「" + destination +"」設定しました。");
    }

    public void searchSpots(String id, String text) throws IOException, InterruptedException, ApiException {
        var session = setupScheduleService.getSession(id);
        if(session == null) {
            send(id, "予定を立てていません。");
            return;
        }
        if(session.getLocation() == null) {
            send(id, "活動場所を教えてください。\n 例: \n @bot \n 渋谷で遊びたい！");
            return;
        }
        if(text == null) {
            send(id, "何をしたいか教えてください。\n 例: \n @bot \n 焼肉食べたい！");
            return;
        }
        ShopCategory category = functionCallService.pickup(text);
        send(id, "「" + session.getLocation() + "」周辺の" + "「" + category.getValue() + "」" + "を調査します。");
        var results = Arrays.stream(googleMapsService.getShopInfo(session.getLocation(), category)).limit(5).toList();
        session.setResults(results);
        send(id, results.stream().map(place -> place.name).collect(Collectors.joining("\n")));
    }

    public void removeSpot(String id, String messageText) {
        if (!setupScheduleService.isStarted(id)) {
            send(id, "予定を立てていません。");
            return;
        }
        var session = setupScheduleService.getSession(id);
        if (session.getResults() == null) {
            send(id, "調査結果がありません。");
            return;
        }
        var command = functionCallService.removeSpot(messageText, session.getResults());
        if (command == null) {
            send(id, "入力内容を正しく認識できませんでした。");
            return;
        }
        if(command.getIndex() == -1) {
            send(id, "入力エラーです。");
            return;
        }
        session.getResults().remove(command.getIndex());
        send(id, "削除しました。");
        send(id, session.getResults().stream().map(place -> place.name).collect(Collectors.joining("\n")));
    }

    public void adopt(String id) {
        if (!setupScheduleService.isStarted(id)) {
            send(id, "予定を立てていません。");
            return;
        }
        var session = setupScheduleService.getSession(id);
        if (session.getResults() == null) {
            send(id, "調査結果がありません。");
            return;
        }
        session.adopt();
        send(id, "調査結果を採用しました。");
    }

    public void draft(String id) {
        if (!setupScheduleService.isStarted(id)) {
            send(id, "予定を立てていません。");
            return;
        }
        var session = setupScheduleService.getSession(id);
        if (session.getActions() == null) {
            send(id, "調査結果がありません。");
            return;
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

    public void decideDraft(String id, String messageText) {
        if (!setupScheduleService.isStarted(id)) {
            send(id, "予定を立てていません。");
            return;
        }
        var session = setupScheduleService.getSession(id);
        if (session.getDrafts() == null) {
            send(id, "草案がありません。");
            return;
        }
        var command = functionCallService.decideDraft(messageText, session.getDrafts());
        if (command == null) {
            send(id, "入力内容を正しく認識できませんでした。");
            return;
        }
        if(command.getIndex() == -1) {
            send(id, "入力エラーです。");
            return;
        }
        session.setActions(session.getDrafts().get(command.getIndex()));
        send(id, "草案を確定しました。");
    }

    public void editAndAddSpotFromDecidedDraft(String id, String messageText) throws IOException, InterruptedException, ApiException {
        if (!setupScheduleService.isStarted(id)) {
            send(id, "予定を立てていません。");
            return;
        }
        var session = setupScheduleService.getSession(id);
        if (session.getActions() == null) {
            send(id, "草案を確定させていません。");
            return;
        }
        var command = functionCallService.editAndAddSpotToDecidedDraft(messageText, session.getActions());
        if (command == null) {
            send(id, "入力内容を正しく認識できませんでした。");
            return;
        }
        if(command.getIndex() == -1) {
            send(id, "入力エラーです。追加する場所が指定されていません。");
            return;
        }
        if(command.getName() == null) {
            send(id, "入力エラーです。追加する場所が指定されていません。");
            return;
        }
        var results = googleMapsService.getShopInfo(session.getLocation(), new ShopCategory(command.getName()));
        if (results.length == 0) {
            send(id, session.getLocation() + "近辺の" + command.getName() + "は見つかりませんでした。");
            return;
        }
        var action = new Action();
        action.setName(results[0].name);
        action.setLocation(results[0].formattedAddress);
        session.getActions().add(command.getIndex(), action);
    }

    public void editAndRemoveSpotFromDecidedDraft(String id, String messageText) {
        if (!setupScheduleService.isStarted(id)) {
            send(id, "予定を立てていません。");
            return;
        }
        var session = setupScheduleService.getSession(id);
        if (session.getActions() == null) {
            send(id, "草案を確定させていません。");
            return;
        }
        var command = functionCallService.editAndRemoveSpotFromDecidedDraft(messageText, session.getActions());
        if (command == null) {
            send(id, "入力内容を正しく認識できませんでした。");
            return;
        }
        if(command.getIndex() == -1) {
            send(id, "入力エラーです。削除する場所が指定されていません。");
            return;
        }
        session.getActions().remove(command.getIndex());
    }

    public void editAndChangeSpotFromDecidedDraft(String id, String messageText) {
        if (!setupScheduleService.isStarted(id)) {
            send(id, "予定を立てていません。");
            return;
        }
        var session = setupScheduleService.getSession(id);
        if (session.getActions() == null) {
            send(id, "草案を確定させていません。");
            return;
        }
        var command = functionCallService.editAndChangeSpotFromDecidedDraft(messageText, session.getActions());
        if (command == null) {
            send(id, "入力内容を正しく認識できませんでした。");
            return;
        }
        if(command.getFromIndex() == -1) {
            send(id, "入力エラーです。変更する場所が指定されていません。");
            return;
        }
        if(command.getToIndex() == -1) {
            send(id, "入力エラーです。変更する場所が指定されていません。");
            return;
        }
        var action = session.getActions().get(command.getFromIndex());
        session.getActions().remove(command.getFromIndex());
        session.getActions().add(command.getToIndex(), action);
    }

//    @EventMapping
    public TextMessage handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws InterruptedException, IOException, ApiException {
        Source source = event.getSource();
        var messageText = event.getMessage().getText();
        if (!messageText.contains("@Moon")) return null;
        String[] lines = messageText.split("\n");
        String contentText = Arrays.stream(lines).skip(1).collect(Collectors.joining("\n"));
        String[] args = Arrays.stream(lines[0].split(" ")).skip(1).toArray(String[]::new);

        if (!(source instanceof GroupSource groupSource)) return null;

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
                ShopCategory category = functionCallService.pickup(args[1]);
                send(id, "「" + session.getLocation() + "」周辺の" + "「" + category.getValue() + "」" + "を調査します。");
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
//            case "追加" -> {
//                if (!setupScheduleService.isStarted(id)) {
//                    send(id, "予定を立てていません。");
//                    return null;
//                }
//                var session = setupScheduleService.getSession(id);
//                session.getActions();
//            }
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
                send(id, "草案を確定しました。");
            }
            case "編集追加" -> {
                if (!setupScheduleService.isStarted(id)) {
                    send(id, "予定を立てていません。");
                    return null;
                }
                var session = setupScheduleService.getSession(id);
                if (session.getActions() == null) {
                    send(id, "草案を確定させていません。");
                    return null;
                }
                int index;
                try {
                    index = Integer.parseInt(args[1]);
                } catch (NumberFormatException exception) {
                    send(id, "数字を入力してください。");
                    return null;
                }
                if (index < 0 || index > session.getActions().size()) {
                    send(id, "数字が範囲外です。");
                    return null;
                }
                String name = args[2];
                var results = googleMapsService.getShopInfo(session.getLocation(), new ShopCategory(name));
                if (results.length == 0) {
                    send(id, session.getLocation() + "近辺の" + name + "は見つかりませんでした。");
                    return null;
                }
                var action = new Action();
                action.setName(results[0].name);
                action.setLocation(results[0].formattedAddress);
                session.getActions().add(index, action);
            }
            case "編集削除" -> {
                if (!setupScheduleService.isStarted(id)) {
                    send(id, "予定を立てていません。");
                    return null;
                }
                var session = setupScheduleService.getSession(id);
                if (session.getActions() == null) {
                    send(id, "草案を確定させていません。");
                    return null;
                }
                int index;
                try {
                    index = Integer.parseInt(args[1]);
                } catch (NumberFormatException exception) {
                    send(id, "数字を入力してください。");
                    return null;
                }
                if (index < 0 || index >= session.getActions().size()) {
                    send(id, "数字が範囲外です。");
                    return null;
                }
                session.getActions().remove(index);
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
        return null;
    }

    private void send(String groupId, String text) {
        var textMessage = TextMessage.builder().text(text).build();
        var pushMessage = new PushMessage(groupId, textMessage);
        try {
            BotApiResponse responce = lineMessagingClient.pushMessage(pushMessage).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}

