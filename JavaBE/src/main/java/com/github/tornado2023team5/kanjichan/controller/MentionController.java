package com.github.tornado2023team5.kanjichan.controller;

import com.github.tornado2023team5.kanjichan.entity.Action;
import com.github.tornado2023team5.kanjichan.model.GroupUserRegistry;
import com.github.tornado2023team5.kanjichan.model.function.CommandInformationFormat;
import com.github.tornado2023team5.kanjichan.model.function.CommandType;
import com.github.tornado2023team5.kanjichan.model.function.ShopCategory;
import com.github.tornado2023team5.kanjichan.model.function.command.*;
import com.github.tornado2023team5.kanjichan.service.*;
import com.github.tornado2023team5.kanjichan.service.mention.*;
import com.google.maps.errors.ApiException;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
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
    private final CommandSetDestinationService commandSetDestinationService;
    private final CommandSearchSpotsService commandSearchSpotsService;
    private final CommandConfirmPlanService commandConfirmPlanService;
    private final CommandMakePlanService commandMakePlanService;
    private final CommandResetPlanService commandResetPlanService;
    private final CommandShowAdoptedSpotsService commandShowAdoptedSpotsService;

    @EventMapping
    public TextMessage formatInput(MessageEvent event) throws InterruptedException, IOException, ApiException, ExecutionException {
        Source source = event.getSource();
        var message = event.getMessage();
        if (!(source instanceof GroupSource groupSource)) return null;

        setupScheduleService.registerUser(new GroupUserRegistry(groupSource.getGroupId(), source.getUserId()));

        if (!(message instanceof TextMessageContent textContent)) return null;
        var messageText = textContent.getText();
        StringBuilder reply = new StringBuilder();

        if (!messageText.contains("@Moon")) return null;

        String[] lines = messageText.split("\n");
        String contentText = Arrays.stream(lines).skip(1).collect(Collectors.joining("\n"));
        String[] args = Arrays.stream(lines[0].split(" ")).skip(1).toArray(String[]::new);

        String id = groupSource.getGroupId();
        CommandInformationFormat format = functionCallService.detect(messageText.replace("@Moon", ""), commandList(id));
        switch (format.getCommandType()) {
            case NONE -> {
                reply.append("入力内容を正しく認識できませんでした。");
                return new TextMessage(reply.toString());
            }
            case MAKE_PLAN -> {
                var command = functionCallService.makePlan(messageText);
                if (command == null) {
                    reply.append("入力内容を正しく認識できませんでした。");
                    return new TextMessage(reply.toString());
                }
                if (setupScheduleService.isEditting(id)) {
                    reply.append("既に予定を立てているウサ！🥕　確定するウサ！🥕\n");
                    return new TextMessage(reply.toString());
                }
                commandMakePlanService.makePlan(command, id, source.getUserId(), reply);
            }
            case RESET_PLAN -> {
                var session = setupScheduleService.getSession(id);
                if (session == null) {
                    reply.append("まずは予定を立てるウサ！🥕\n");
                    return new TextMessage(reply.toString());
                }
                commandResetPlanService.resetPlan(id, reply);
            }
            case CONFIRM_PLAN -> {
                var session = setupScheduleService.getSession(id);
                if (session == null) {
                    reply.append("まずは予定を立てるウサ！🥕\n");
                    return new TextMessage(reply.toString());
                }
                if (session.getResultsList().isEmpty()) {
                    reply.append("何をして遊ぶかを教えるウサ！🥕\n");
                    return new TextMessage(reply.toString());
                }
                commandConfirmPlanService.confirmPlan(id, reply);
            }
            case SET_LOCATION -> {
                var command = functionCallService.setLocation(messageText);
                if (command == null) return new TextMessage(reply + "入力内容を正しく認識できませんでした。");
                var session = setupScheduleService.getSession(id);
                if (session == null) {
                    reply.append("まずは予定を立てるウサ！🥕\n");
                    return new TextMessage(reply.toString());
                }
                if (command.getDestination() == null) {
                    reply.append("集合場所場所を教えるウサ！🥕\n 例: \n @Moon \n 渋谷でカラオケする予定を立てて！");
                    return new TextMessage(reply.toString());
                }
                commandSetDestinationService.setDestination(id, reply, command.getDestination(), true);
            }
            case SEARCH_SPOTS -> {
                var command = functionCallService.searchSpots(messageText);
                if (command == null) return new TextMessage(reply + "入力内容を正しく認識できませんでした。");
                if (!setupScheduleService.isEditting(id)) {
                    reply.append("まずは予定を立てるウサ！🥕\n");
                    return new TextMessage(reply.toString());
                }
                var session = setupScheduleService.getSession(id);
                if (session.getResultsList().isEmpty()) {
                    reply.append("遊ぶ内容を教えてほしいウサ！🥕");
                    return new TextMessage(reply.toString());
                }
                commandSearchSpotsService.searchSpots(id, reply, command.getCategory(), command.getDestination());
            }
            case SHOW_ADOPTED_SPOTS -> commandShowAdoptedSpotsService.showAdoptedSpots(id, reply);
            case SET_TIME -> {
                var command = functionCallService.setTime(messageText);
            }
        }
        return new TextMessage(reply.toString());
    }

//    public TextMessage validate(CommandType type, CommandModel model, StringBuilder reply) {
//        // switch 内パターンマッチ使いてぇ！!Kotlin JVM21への対応はよ！
//        switch (type) {
//            case NONE -> reply.append("入力内容を正しく認識できなかったウサ！🥕");
//            case MAKE_PLAN -> {
//                var command = functionCallService.makePlan(messageText);
//                if (command == null) return new TextMessage(reply + "入力内容を正しく認識できませんでした。");
//                commandMakePlanService.makePlan(id, reply, command, source.getUserId());
//            }
//            case RESET_PLAN -> {
//                var session = setupScheduleService.getSession(id);
//                if (session == null) {
//                    reply.append("まずは予定を立てるウサ！🥕\n");
//                    return new TextMessage(reply.toString());
//                }
//                commandResetPlanService.resetPlan(id, reply);
//            }
//            case CONFIRM_PLAN -> {
//                var session = setupScheduleService.getSession(id);
//                if (session == null) {
//                    reply.append("まずは予定を立てるウサ！🥕\n");
//                    return new TextMessage(reply.toString());
//                }
//                if (session.getResultsList().isEmpty()) {
//                    reply.append("何をして遊ぶかを教えるウサ！🥕\n");
//                    return new TextMessage(reply.toString());
//                }
//                commandConfirmPlanService.confirmPlan(id, reply);
//            }
//            case SET_LOCATION -> {
//                var command = functionCallService.setLocation(messageText);
//                if (command == null) return new TextMessage(reply + "入力内容を正しく認識できませんでした。");
//                var session = setupScheduleService.getSession(id);
//                if (session == null) {
//                    reply.append("まずは予定を立てるウサ！🥕\n");
//                    return new TextMessage(reply.toString());
//                }
//                if (command.getDestination() == null) {
//                    reply.append("集合場所場所を教えるウサ！🥕\n 例: \n @Moon \n 渋谷でカラオケする予定を立てて！");
//                    return new TextMessage(reply.toString());
//                }
//                commandSetDestinationService.setDestination(id, reply, command.getDestination(), true);
//            }
//            case SEARCH_SPOTS -> {
//                var command = functionCallService.searchSpots(messageText);
//                if (command == null) return new TextMessage(reply + "入力内容を正しく認識できませんでした。");
//                commandSearchSpotsService.searchSpots(id, reply, command.getCategory(), command.getDestination());
//            }
//            case SHOW_ADOPTED_SPOTS -> commandShowAdoptedSpotsService.showAdoptedSpots(id, reply);
//        }
//        return new TextMessage(reply.toString());
//    }

    public String commandList(String id) {
        StringBuilder complete = new StringBuilder();
        complete.append("あなたは旅行者の旅行計画を補助するBOTです。\n");
        complete.append("ユーザーの入力がコマンドのどれに当てはまるか分類してください。\n");
        complete.append("下記のコマンドの候補にないものは現在ユーザーが入力することはできません。\n");
        if (!setupScheduleService.isEditting(id)) {
            complete.append("MAKE_PLAN: 旅行計画を作成します。「活動場所と遊び内容が含まれている」もしくは「予定を立てる旨の指示」の時選択してください。例: 「渋谷で焼肉」「～予定を立ててください！」「～で遊びたい！」\n");
            complete.append("NONE: どのコマンドにも当てはまらない場合です。\n");
            return complete.toString();
        }
        var session = setupScheduleService.getSession(id);

        complete.append("SET_LOCATION: 計画の目的地、集合場所を設定します。\n");
        if (session.getLocation() != null)
            complete.append("SEARCH_SPOTS: 計画の観光スポット、遊び場を検索します。「～で遊びたい！」\n");
        if (session.getResultsList().size() >= 1)
            complete.append("SHOW_ADOPTED_SPOTS: 採用した観光スポット、遊び場をすべて表示します。\n");

        if (session.getActions() != null)
            complete.append("CONFIRM_PLAN: 旅行計画を確定します。\n");

        complete.append("RESET_PLAN: 現在計画中の旅行計画をリセットします。\n");
        complete.append("NONE: どのコマンドにも当てはまらず、入力が不正と思われるもの。\n");

        return complete.toString();
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
        if (session.getResults().isEmpty()) {
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
//        session.getResultsList().add(session.getResults());
        session.setResults(null);
        reply.append("調査結果を採用しました。");
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
        if (results.size() == 0) {
            reply.append(session.getLocation()).append("近辺の").append(command.getName()).append("は見つかりませんでした。");
            return;
        }
        var action = new Action();
        action.setName(results.get(0).name);
        action.setLocation(results.get(0).formattedAddress);
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
        if (session.getResults().isEmpty()) {
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

