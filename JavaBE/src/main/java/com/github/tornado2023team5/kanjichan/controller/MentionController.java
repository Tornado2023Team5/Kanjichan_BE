package com.github.tornado2023team5.kanjichan.controller;

import com.github.tornado2023team5.kanjichan.entity.Action;
import com.github.tornado2023team5.kanjichan.model.AsobiPlanningSession;
import com.github.tornado2023team5.kanjichan.model.function.CommandInformationFormat;
import com.github.tornado2023team5.kanjichan.model.function.ShopCategory;
import com.github.tornado2023team5.kanjichan.model.function.command.*;
import com.github.tornado2023team5.kanjichan.service.*;
import com.google.maps.errors.ApiException;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.profile.MembersIdsResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    public TextMessage formatInput(MessageEvent event) throws InterruptedException, IOException, ApiException, ParseException {
        Source source = event.getSource();
        var message = event.getMessage();
        if(!(message instanceof TextMessageContent textContent)) return null;
        var messageText = textContent.getText();
        StringBuilder reply = new StringBuilder();

        if (!messageText.contains("@Moon")) return null;

        String[] lines = messageText.split("\n");
        String contentText = Arrays.stream(lines).skip(1).collect(Collectors.joining("\n"));
        String[] args = Arrays.stream(lines[0].split(" ")).skip(1).toArray(String[]::new);


        if (!(source instanceof GroupSource groupSource)) return null;

        String id = groupSource.getGroupId();
        CommandInformationFormat format = functionCallService.detect(messageText.replace("@Moon", ""), commandList(id));
        reply.append(format.getCommandType()).append("\n\n");
        switch (format.getCommandType()) {
            case NONE -> reply.append("入力内容を正しく認識できませんでした。");
            case JOIN_PLAN -> joinPlan(id, reply, source.getUserId());
            case MAKE_PLAN -> {
                var command = functionCallService.makePlan(messageText);
                if (command == null) return new TextMessage(reply + "入力内容を正しく認識できませんでした。");
                makePlan(id, reply, command, source.getUserId());
            }
            case RESET_PLAN -> resetPlan(id, reply);
            case CONFIRM_PLAN -> confirmPlan(id, reply);
            case SET_LOCATION -> {
                var command = functionCallService.setLocation(messageText);
                if (command == null) return new TextMessage(reply + "入力内容を正しく認識できませんでした。");
                setDestination(id, reply, command.getDestination());
            }
            case SEARCH_SPOTS -> {
                var command = functionCallService.searchSpots(messageText);
                if (command == null) return new TextMessage(reply + "入力内容を正しく認識できませんでした。");
                searchSpots(id, reply, command.getCategory());
            }
//            case REMOVE_SPOT -> removeSpot(id, reply, messageText);
//            case ADOPT_SPOTS -> adopt(id, reply);
            case SHOW_ADOPTED_SPOTS -> showAdoptedSpots(id, reply);
//            case MAKE_DRAFT -> draft(id, reply);
//            case DECIDE_DRAFT -> decideDraft(id, reply, messageText);
        }
        return new TextMessage(reply.toString());
    }

    public String commandList(String id) {
        StringBuilder complete = new StringBuilder();
        complete.append("あなたは旅行者の旅行計画を補助するBOTです。\n");
        complete.append("ユーザーの入力がコマンドのどれに当てはまるか分類してください。\n");
        complete.append("下記のコマンドの候補にないものは現在ユーザーが入力することはできません。\n");
        if(!setupScheduleService.isEditting(id)) {
            complete.append("MAKE_PLAN: 旅行計画を作成します。\n");
            complete.append("NONE: どのコマンドにも当てはまらない場合です。\n");
            return complete.toString();
        }
        var session = setupScheduleService.getSession(id);

        complete.append("SET_LOCATION: 計画の目的地、集合場所を設定します。\n");
        complete.append("JOIN_PLAN: 旅行計画、遊び計画のメンバーに参加します。\n");
        complete.append("SEARCH_SPOTS: 計画の観光スポット、遊び場を検索します。\n");
        complete.append("SHOW_ADOPTED_SPOTS: 採用した観光スポット、遊び場をすべて表示します。\n");

        if(session.getActions() != null)
            complete.append("CONFIRM_PLAN: 旅行計画を確定します。\n");

        complete.append("RESET_PLAN: 現在計画中の旅行計画をリセットします。\n");
        complete.append("NONE: どのコマンドにも当てはまらず、入力が不正と思われるもの。\n");

        return complete.toString();
    }

    public void joinPlan(String id, StringBuilder reply, String lineId) throws IOException, InterruptedException, ApiException {
        var session = setupScheduleService.getSession(id);
        if (session == null) {
            reply.append("予定を立てていません。");
            return;
        }
        setupScheduleService.addUser(id, lineId);
        reply.append("予定の参加者として登録しました。\n");
    }

    public void makePlan(String id, StringBuilder reply, MakePlanCommand command, String lineId) throws IOException, InterruptedException, ApiException {
        if (setupScheduleService.isEditting(id)) {
            reply.append("既に予定を立てています。");
            return;
        }
        setupScheduleService.start(id, lineId);
        reply.append("予定を立てる準備をしました。\n");
        if(command.getDestination() != null) setDestination(id, reply, command.getDestination());
        else reply.append("集合場所を教えてください。\n");
        if(command.getCategory() != null) searchSpots(id, reply, command.getCategory());
        else reply.append("何をして遊ぶかを教えてください。\n");
    }

    public void resetPlan(String id, StringBuilder reply) {
        var session = setupScheduleService.getSession(id);
        if (session == null) {
            reply.append("予定を立てていません。");
            return;
        }
        setupScheduleService.reset(id);
        reply.append("編集中の予定をリセットし、全ての情報を削除しました。\n");
    }

    public void confirmPlan(String id, StringBuilder reply) throws IOException, InterruptedException, ApiException {
        var session = setupScheduleService.getSession(id);
        if (session == null) {
            reply.append("予定を立てていません。");
            return;
        }
        if (session.getResultsList().size() == 0) {
            reply.append("採用した調査結果がありません。");
            return;
        }
        setupScheduleService.draft(id);
        reply.append(session.getDrafts().get(0).stream().map(Action::getName).collect(Collectors.joining("\n↓\n")));
        setupScheduleService.decideDraft(session, session.getDrafts().get(0));
        setupScheduleService.confirm(id);
        reply.append("遊び計画を確定しました。良い一日を！");
    }

    public void setDestination(String id, StringBuilder reply, String destination) {
        var session = setupScheduleService.getSession(id);
        if (session == null) {
            reply.append("予定を立てていません。");
            return;
        }
        if (destination == null) {
            reply.append("活動場所を教えてください。\n 例: \n @bot \n 渋谷で遊びたい！");
            return;
        }
        setupScheduleService.setLocation(id, destination);
        reply.append("活動場所を「").append(destination).append("」に設定しました。\n");
    }

    public void searchSpots(String id, StringBuilder reply, String text) throws IOException, InterruptedException, ApiException {
        var session = setupScheduleService.getSession(id);
        if (session == null) {
            reply.append("予定を立てていません。");
            return;
        }
        if (session.getLocation() == null) {
            reply.append("活動場所を設定してください。\n 例: \n @bot \n 渋谷で遊びたい！");
            return;
        }
        if (text == null) {
            reply.append("何をしたいか教えてください。\n 例: \n @bot \n 焼肉食べたい！");
            return;
        }
        ShopCategory category = functionCallService.pickup(text);
        reply.append("「").append(session.getLocation()).append("」周辺の").append("「").append(category.getValue()).append("」").append("を調査します。\n\n");
        var results = new ArrayList<>(Arrays.stream(googleMapsService.getShopInfo(session.getLocation(), category)).limit(3).toList());
        session.setResults(results);
        reply.append(results.stream().map(place -> place.name).collect(Collectors.joining("\n")));
        session.getResultsList().add(session.getResults());
        session.setResults(null);
        reply.append("他にも遊び場所を調査しますか？\n").append("調査しない場合は予定を確定してください。\n");
    }

    public void removeSpot(String id, StringBuilder reply, String messageText) {
        if (!setupScheduleService.isEditting(id)) {
            reply.append("予定を立てていません。");
            return;
        }
        var session = setupScheduleService.getSession(id);
        var command = functionCallService.removeSpot(messageText, session.getResults());
        if (command == null) {
            reply.append("入力内容を正しく認識できませんでした。");
            return;
        }
        if (command.getSpots() == null || command.getSpots().isEmpty()) {
            reply.append("調査結果に該当するスポットがありません。");
            return;
        }
        session.getResults().removeIf(place -> command.getSpots().contains(place.name));
        reply.append(String.join(",", command.getSpots())).append("を削除しました。\n");
        if(session.getResults().isEmpty()) {
            reply.append("調査結果をすべて削除しました。\n");
            session.setResults(null);
        } else {
            reply.append("削除した結果以下の候補が残りました。採用するか、不要なものを削除してください。\n");
            reply.append(session.getResults().stream().map(place -> place.name).collect(Collectors.joining("\n")));
        }
    }

    public void adopt(String id, StringBuilder reply) {
        if (!setupScheduleService.isEditting(id)) {
            reply.append("予定を立てていません。");
            return;
        }
        var session = setupScheduleService.getSession(id);
        if (session.getResults() == null) {
            reply.append("調査結果がありません。");
            return;
        }
        session.getResultsList().add(session.getResults());
        session.setResults(null);
        reply.append("調査結果を採用しました。");
    }

    public void showAdoptedSpots(String id, StringBuilder reply) {
        if (!setupScheduleService.isEditting(id)) {
            reply.append("予定を立てていません。");
            return;
        }
        var session = setupScheduleService.getSession(id);
        if (session.getResultsList().size() == 0) {
            reply.append("採用した調査結果がありません。");
            return;
        }
        reply.append("採用したスポットは以下の通りです。\n");
        for(var results : session.getResultsList()) {
            for(var result : results) {
                reply.append(result.name).append("\n");
            }
            reply.append("\n");
        }
    }

    public void draft(String id, StringBuilder reply) throws IOException, InterruptedException, ApiException {
        if (!setupScheduleService.isEditting(id)) {
            reply.append("予定を立てていません。");
            return;
        }
        var session = setupScheduleService.getSession(id);
        if (session.getResultsList().size() == 0) {
            reply.append("採用した調査結果がありません。");
            return;
        }
        reply.append("草案を作成しました。\n");
        reply.append("採用する案を決めてください。\n");

        setupScheduleService.draft(id);
//        for (int i = 0; i < session.getDrafts().size(); i++) {
//            reply.append("草案").append(i + 1).append(":\n");
//            reply.append(session.getDrafts().get(i).stream().map(Action::getName).collect(Collectors.joining("\n↓\n")));
//            reply.append("\n\n");
//        }
        reply.append(session.getDrafts().get(0).stream().map(Action::getName).collect(Collectors.joining("\n↓\n")));
        setupScheduleService.decideDraft(session, session.getDrafts().get(0));
        reply.append("遊び計画を確定しました。良い一日を！");
    }

    public void decideDraft(String id, StringBuilder reply, String messageText) throws IOException, InterruptedException, ApiException {
        if (!setupScheduleService.isEditting(id)) {
            reply.append("予定を立てていません。");
            return;
        }
        var session = setupScheduleService.getSession(id);
        if (session.getDrafts() == null) {
            reply.append("草案がありません。");
            return;
        }
        var command = functionCallService.decideDraft(messageText, session.getDrafts());
        if (command == null) {
            reply.append("入力内容を正しく認識できませんでした。");
            return;
        }
        if (command.getIndex() <= 0) {
            reply.append("該当する予定がありません。");
            return;
        }
        setupScheduleService.decideDraft(session, session.getDrafts().get(command.getIndex() - 1));
        reply.append("遊び計画を確定しました。良い一日を！");
    }

    public void editAndAddSpotFromDecidedDraft(String id, StringBuilder reply, String messageText) throws IOException, InterruptedException, ApiException {
        if (!setupScheduleService.isEditting(id)) {
            reply.append("予定を立てていません。");
            return;
        }
        var session = setupScheduleService.getSession(id);
        if (session.getActions() == null) {
            reply.append("草案を確定させていません。");
            return;
        }
        var command = functionCallService.editAndAddSpotToDecidedDraft(messageText, session.getActions());
        if (command == null) {
            reply.append("入力内容を正しく認識できませんでした。");
            return;
        }
        if (command.getIndex() <= -1) {
            reply.append("追加する場所が指定されていません。");
            return;
        }
        if (command.getName() == null) {
            reply.append("追加する場所が指定されていません。");
            return;
        }
        var results = googleMapsService.getShopInfo(session.getLocation(), new ShopCategory(command.getName()));
        if (results.length == 0) {
            reply.append(session.getLocation()).append("近辺の").append(command.getName()).append("は見つかりませんでした。");
            return;
        }
        var action = new Action();
        action.setName(results[0].name);
        action.setLocation(results[0].formattedAddress);
        session.getActions().add(command.getIndex(), action);
        reply.append("追加しました。\n");
        reply.append(session.getActions().stream().map(Action::getName).collect(Collectors.joining("\n")));
    }

    public void editAndRemoveSpotFromDecidedDraft(String id, StringBuilder reply, String messageText) {
        if (!setupScheduleService.isEditting(id)) {
            reply.append("予定を立てていません。");
            return;
        }
        var session = setupScheduleService.getSession(id);
        if (session.getActions() == null) {
            reply.append("草案を確定させていません。");
            return;
        }
        var command = functionCallService.editAndRemoveSpotFromDecidedDraft(messageText, session.getActions());
        if (command == null) {
            reply.append("入力内容を正しく認識できませんでした。");
            return;
        }
        if (command.getSpots() == null || command.getSpots().isEmpty()) {
            reply.append("削除する場所が指定されていません。");
            return;
        }
        session.getActions().removeIf(action -> command.getSpots().contains(action.getName()));
        reply.append("削除しました");
        reply.append(session.getActions().stream().map(Action::getName).collect(Collectors.joining("\n")));


        reply.append(String.join(",", command.getSpots())).append("を削除しました。\n");
        if(session.getResults().isEmpty()) {
            reply.append("草案を破棄しました。\n");
            session.setActions(null);
        } else {
            reply.append("削除した結果以下の草案になりました。確定、スポット追加、スポット削除、順番入れ替えの中から選んで操作してください\n");
            reply.append(session.getResults().stream().map(place -> place.name).collect(Collectors.joining("\n")));
        }
    }

    public void editAndChangeSpotFromDecidedDraft(String id, StringBuilder reply, String messageText) {
        if (!setupScheduleService.isEditting(id)) {
            reply.append("予定を立てていません。");
            return;
        }
        var session = setupScheduleService.getSession(id);
        if (session.getActions() == null) {
            reply.append("草案を確定させていません。");
            return;
        }
        var command = functionCallService.editAndChangeSpotFromDecidedDraft(messageText, session.getActions());
        if (command == null) {
            reply.append("入力内容を正しく認識できませんでした。");
            return;
        }
        if (command.getFromIndex() == -1) {
            reply.append("変更する場所が指定されていません。");
            return;
        }
        if (command.getToIndex() == -1) {
            reply.append("変更先の場所が指定されていません。");
            return;
        }
        var action = session.getActions().get(command.getFromIndex());
        session.getActions().remove(command.getFromIndex());
        session.getActions().add(command.getToIndex(), action);
        reply.append("入れ替えました\n");
        reply.append(session.getActions().stream().map(Action::getName).collect(Collectors.joining("\n")));
    }

    private void send(String replyToken, List<String> texts) {
        lineMessagingClient.replyMessage(new ReplyMessage(replyToken, texts.stream().map(TextMessage::new).collect(Collectors.toList())));
    }
}

