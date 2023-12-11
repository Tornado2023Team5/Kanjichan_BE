package com.github.tornado2023team5.kanjichan.controller;

import com.github.tornado2023team5.kanjichan.entity.Action;
import com.github.tornado2023team5.kanjichan.model.AsobiPlanningSession;
import com.github.tornado2023team5.kanjichan.model.GroupUserRegistry;
import com.github.tornado2023team5.kanjichan.model.function.CommandInformationFormat;
import com.github.tornado2023team5.kanjichan.model.function.ShopCategory;
import com.github.tornado2023team5.kanjichan.model.function.command.*;
import com.github.tornado2023team5.kanjichan.service.*;
import com.google.maps.errors.ApiException;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.UserSource;

import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.profile.MembersIdsResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.LocalDateTime;
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
    private final String[] liffCommands = {"クーポン", "使い方"};

    @EventMapping
    public TextMessage formatInput(MessageEvent event)
            throws InterruptedException, IOException, ApiException, ExecutionException {
        Source source = event.getSource();
        var message = event.getMessage();

        if (!(message instanceof TextMessageContent textContent))
            return null;
        var messageText = textContent.getText();

        StringBuilder reply = new StringBuilder();
        if (source instanceof UserSource) {
            String incomingMessage = textContent.getText();
            String replyToken = event.getReplyToken();

            // LIFFコマンドが含まれているかチェック
            boolean containsLiffCommand =
                    Arrays.stream(liffCommands).anyMatch(incomingMessage::contains);

            if (!containsLiffCommand) {
                // ユーザーへ返信するメッセージを生成
                String replyMessage = "受け取ったメッセージ: " + incomingMessage;
                // LINE APIを使用してメッセージを返信
                lineMessagingClient
                        .replyMessage(new ReplyMessage(replyToken, new TextMessage(replyMessage)))
                        .get();
            }
        }
        if (!(source instanceof GroupSource groupSource))
            return null;

        setupScheduleService
                .registerUser(new GroupUserRegistry(groupSource.getGroupId(), source.getUserId()));

        if (!messageText.contains("@Moon"))
            return null;

        String groupId = groupSource.getGroupId();

        CommandInformationFormat format =
                functionCallService.detect(messageText.replace("@Moon", ""), commandList(groupId));

        switch (format.getCommandType()) {
            case NONE -> reply.append("入力内容を正しく認識できなかったウサ！🥕");
            case MAKE_PLAN -> {
                var command = functionCallService.makePlan(messageText);
                if (command == null)
                    return new TextMessage(reply + "入力内容を正しく認識できませんでした。");

                makePlan(groupId, reply, command, source.getUserId());
            }
            case RESET_PLAN -> resetPlan(groupId, reply);
            case CONFIRM_PLAN -> confirmPlan(groupId, reply);
            case SET_LOCATION -> {
                var command = functionCallService.setLocation(messageText);
                if (command == null)
                    return new TextMessage(reply + "入力内容を正しく認識できませんでした。");
                setDestination(groupId, reply, command.getDestination(), true);
            }
            case SEARCH_SPOTS -> {
                var command = functionCallService.searchSpots(messageText);
                if (command == null)
                    return new TextMessage(reply + "入力内容を正しく認識できませんでした。");
                searchSpots(groupId, reply, command.getCategory(), command.getDestination());
            }
            case SHOW_ADOPTED_SPOTS -> showAdoptedSpots(groupId, reply);
        }

        // TextMessage grorpReplyMessage = generateGroupReplyText(messageText);

        return new TextMessage(reply.toString());
    }

    // text 処理を分割したい。 textのみでテストを書きたい

    public String commandList(String id) {
        StringBuilder complete = new StringBuilder();
        complete.append("あなたは旅行者の旅行計画を補助するBOTです。\n");
        complete.append("ユーザーの入力がコマンドのどれに当てはまるか分類してください。\n");
        complete.append("下記のコマンドの候補にないものは現在ユーザーが入力することはできません。\n");
        if (!setupScheduleService.isEditting(id)) {
            complete.append(
                    "MAKE_PLAN: 旅行計画を作成します。「活動場所と遊び内容が含まれている」もしくは「予定を立てる旨の指示」の時選択してください。例: 「渋谷で焼肉」「～予定を立ててください！」「～で遊びたい！」\n");
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

    public void createUserSchedule(StringBuilder reply) {
        // check is user schedule exist

        reply.append("すでにスケジュールが入っているウサ\n");

    }

    public void makePlan(String id, StringBuilder reply, MakePlanCommand command, String lineId)
            throws IOException, InterruptedException, ApiException, ExecutionException {
        if (setupScheduleService.isEditting(id)) {
            reply.append("既に予定を立てているウサ！🥕　確定するウサ！🥕\n");
            return;
        }
        setupScheduleService.start(id, lineId,
                lineMessagingClient.getGroupSummary(id).get().getGroupName());
        reply.append("予定を立てる準備をしたウサ！🥕\n");

        if (command.getDestination() != null)
            setDestination(id, reply, command.getDestination(), false);
        else {
            reply.append("集合場所を教えるウサ！🥕\n");
            return;
        }

        if (command.getCategory() != null)
            searchSpots(id, reply, command.getCategory(),
                    setupScheduleService.getSession(id).getLocation());
        else
            reply.append("何をして遊ぶかを教えるウサ！🥕\n");
    }

    public void resetPlan(String id, StringBuilder reply) {
        var session = setupScheduleService.getSession(id);
        if (session == null) {
            reply.append("まずは予定を立てるウサ！🥕\n");
            return;
        }
        setupScheduleService.reset(id);
        reply.append("編集中の予定をリセットして、全ての情報を削除したウサ🥕\n");
    }

    public void confirmPlan(String id, StringBuilder reply)
            throws IOException, InterruptedException, ApiException {
        var session = setupScheduleService.getSession(id);
        if (session == null) {
            reply.append("まずは予定を立てるウサ！🥕\n");
            return;
        }
        if (session.getResultsList().size() == 0) {
            reply.append("何をして遊ぶかを教えるウサ！🥕\n");
            return;
        }
        setupScheduleService.draft(id);
        reply.append(session.getDrafts().get(0).stream().map(Action::getName)
                .collect(Collectors.joining("\n↓\n")));
        setupScheduleService.decideDraft(session, session.getDrafts().get(0));
        session.setUsers(setupScheduleService.getUsers(id).getUserIds());
        LocalDateTime date = setupScheduleService.confirm(id);

        reply.append("予定内容:\n");
        reply.append("◦ 日程: ").append(date).append("\n");
        reply.append("◦ 場所: ").append(googleMapsService.getStation(session.getLocation()).name)
                .append("\n\n");
        reply.append("楽しんできてほしいウサ！\uD83D\uDC30✨");

        var directMessage = new StringBuilder();
        directMessage.append("\uD83D\uDE80\uD83E\uDD55遊びの予定が決まったうさ\uD83E\uDD55\uD83D\uDE80\n");
        directMessage.append("◦ グループ: ").append(session.getName()).append("\n");
        directMessage.append("◦ 日程: ").append(date).append("\n");
        directMessage.append("◦ 場所: ")
                .append(googleMapsService.getStation(session.getLocation()).name).append("\n\n");
        directMessage.append("楽しんできてほしいウサ！\uD83D\uDC30✨");

        var lineUsers = setupScheduleService.getLineUsers(id);
        var googleUsers = setupScheduleService.getGoogleCalendarUsers(id);
        var addCalendarText = setupScheduleService.createEventUrl(session.getName(), "",
                session.getActions().get(0).getLocation(), session.getActions().get(0).getStart(),
                session.getActions().get(session.getActions().size() - 1).getEnd());

        lineUsers.getLineUserIds().forEach(userId -> {
            var textMessage = new TextMessage(directMessage.toString());
            var addCalendarMessage = new TextMessage(
                    "Googleカレンダーに追加するリンクウサ！忘れないよにするウサ\uD83D\uDCC5" + "\n" + addCalendarText);
            var pushMessage = googleUsers.contains(userId)
                    ? new PushMessage(userId, Arrays.asList(textMessage, addCalendarMessage))
                    : new PushMessage(userId, textMessage);
            lineMessagingClient.pushMessage(pushMessage);
        });
    }

    public void setDestination(String id, StringBuilder reply, String destination,
            boolean sendReply) {
        var session = setupScheduleService.getSession(id);
        if (session == null) {
            reply.append("まずは予定を立てるウサ！🥕\n");
            return;
        }
        if (destination == null) {
            reply.append("集合場所場所を教えるウサ！🥕\n 例: \n @Moon \n 渋谷でカラオケする予定を立てて！");
            return;
        }
        setupScheduleService.setLocation(id, destination);

        if (sendReply)
            reply.append("活動場所を「").append(destination).append("」に設定したウサ！🥕\n");
    }

    public void searchSpots(String id, StringBuilder reply, String text, String location)
            throws IOException, InterruptedException, ApiException {
        var session = setupScheduleService.getSession(id);
        if (session == null) {
            reply.append("まずは予定を立てるウサ！🥕\n");
            return;
        }
        if (session.getLocation() == null && location == null) {
            reply.append("活動場所を設定してください。\n 例: \n @bot \n 渋谷で遊びたい！");
            return;
        }
        if (text == null) {
            reply.append("何をしたいか教えてください。\n 例: \n @bot \n 焼肉食べたい！");
            return;
        }
        if (location != null)
            setDestination(id, reply, location, true);
        ShopCategory category = functionCallService.pickup(text);

        var results = googleMapsService.getShopInfo(session.getLocation(), category);
        session.getResultsList().add(results);

        reply.append("「").append(session.getLocation()).append("」周辺の").append("「")
                .append(category.getValue()).append("」").append("はこんな所があるウサ！！\uD83D\uDC30\n\n");
        reply.append(results.stream()
                .map(place -> "◦ " + place.name + "\n" + "レビュー: "
                        + GoogleMapsService.getRatingStars(place.rating) + " " + place.rating + "\n"
                        + place.url)
                .collect(Collectors.joining("\n\n")));
        reply.append("\n\n").append("他にも遊び場所を追加するウサ？🌕\uD83D\uDC30✨\n")
                .append("予定を確定するなら確定！って言ってほしいウサ！！\uD83E\uDD55\n");
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
            reply.append(session.getResults().stream().map(place -> place.name)
                    .collect(Collectors.joining("\n")));
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
        // session.getResultsList().add(session.getResults());
        session.setResults(null);
        reply.append("調査結果を採用しました。");
    }

    public void showAdoptedSpots(String id, StringBuilder reply) {
        if (!setupScheduleService.isEditting(id)) {
            reply.append("まずは予定を立てるウサ！🥕\n");
            return;
        }
        var session = setupScheduleService.getSession(id);
        if (session.getResultsList().size() == 0) {
            reply.append("遊ぶ内容を教えてほしいウサ！🥕");
            return;
        }
        reply.append("今はここで遊ぶ予定を立てているウサ！🥕");
        for (var results : session.getResultsList()) {
            for (var result : results) {
                reply.append(result.name).append("\n");
            }
            reply.append("\n");
        }
    }

    public void draft(String id, StringBuilder reply)
            throws IOException, InterruptedException, ApiException {
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
        // for (int i = 0; i < session.getDrafts().size(); i++) {
        // reply.append("草案").append(i + 1).append(":\n");
        // reply.append(session.getDrafts().get(i).stream().map(Action::getName).collect(Collectors.joining("\n↓\n")));
        // reply.append("\n\n");
        // }
        reply.append(session.getDrafts().get(0).stream().map(Action::getName)
                .collect(Collectors.joining("\n↓\n")));
        setupScheduleService.decideDraft(session, session.getDrafts().get(0));
        reply.append("遊び計画を確定しました。良い一日を！");
    }

    public void decideDraft(String id, StringBuilder reply, String messageText)
            throws IOException, InterruptedException, ApiException {
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

    public void editAndAddSpotFromDecidedDraft(String id, StringBuilder reply, String messageText)
            throws IOException, InterruptedException, ApiException {
        if (!setupScheduleService.isEditting(id)) {
            reply.append("予定を立てていません。");
            return;
        }
        var session = setupScheduleService.getSession(id);
        if (session.getActions() == null) {
            reply.append("草案を確定させていません。");
            return;
        }
        var command =
                functionCallService.editAndAddSpotToDecidedDraft(messageText, session.getActions());
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
        var results = googleMapsService.getShopInfo(session.getLocation(),
                new ShopCategory(command.getName()));
        if (results.size() == 0) {
            reply.append(session.getLocation()).append("近辺の").append(command.getName())
                    .append("は見つかりませんでした。");
            return;
        }
        var action = new Action();
        action.setName(results.get(0).name);
        action.setLocation(results.get(0).formattedAddress);
        session.getActions().add(command.getIndex(), action);
        reply.append("追加しました。\n");
        reply.append(session.getActions().stream().map(Action::getName)
                .collect(Collectors.joining("\n")));
    }

    public void editAndRemoveSpotFromDecidedDraft(String id, StringBuilder reply,
            String messageText) {
        if (!setupScheduleService.isEditting(id)) {
            reply.append("予定を立てていません。");
            return;
        }
        var session = setupScheduleService.getSession(id);
        if (session.getActions() == null) {
            reply.append("草案を確定させていません。");
            return;
        }
        var command = functionCallService.editAndRemoveSpotFromDecidedDraft(messageText,
                session.getActions());
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
        reply.append(session.getActions().stream().map(Action::getName)
                .collect(Collectors.joining("\n")));

        reply.append(String.join(",", command.getSpots())).append("を削除しました。\n");
        if (session.getResults().isEmpty()) {
            reply.append("草案を破棄しました。\n");
            session.setActions(null);
        } else {
            reply.append("削除した結果以下の草案になりました。確定、スポット追加、スポット削除、順番入れ替えの中から選んで操作してください\n");
            reply.append(session.getResults().stream().map(place -> place.name)
                    .collect(Collectors.joining("\n")));
        }
    }

    public void editAndChangeSpotFromDecidedDraft(String id, StringBuilder reply,
            String messageText) {
        if (!setupScheduleService.isEditting(id)) {
            reply.append("予定を立てていません。");
            return;
        }
        var session = setupScheduleService.getSession(id);
        if (session.getActions() == null) {
            reply.append("草案を確定させていません。");
            return;
        }
        var command = functionCallService.editAndChangeSpotFromDecidedDraft(messageText,
                session.getActions());
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
        reply.append(session.getActions().stream().map(Action::getName)
                .collect(Collectors.joining("\n")));
    }

    private void send(String replyToken, List<String> texts) {
        lineMessagingClient.replyMessage(new ReplyMessage(replyToken,
                texts.stream().map(TextMessage::new).collect(Collectors.toList())));
    }
}
