package com.github.tornado2023team5.kanjichan.controller;

import com.github.tornado2023team5.kanjichan.service.GoogleMapsService;
import com.github.tornado2023team5.kanjichan.service.LineMessageService;
import com.github.tornado2023team5.kanjichan.service.PickUpCategoryService;
import com.github.tornado2023team5.kanjichan.service.SetupScheduleService;
import com.google.maps.errors.ApiException;
import com.linecorp.bot.client.LineMessagingClient;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@LineMessageHandler
@Component
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
        var defaultMessage = new TextMessage(messageText);
        if (!messageText.contains("@" + messageText)) return defaultMessage;
        String[] lines = messageText.split("\n");
        if (lines.length <= 1) {
            reply(event.getReplyToken(), "メンションありがとうございます！");
            return defaultMessage;
        }

        String contentText = Arrays.stream(lines).skip(1).collect(Collectors.joining("\n"));
        String[] args = Arrays.stream(lines[0].split(" ")).skip(1).toArray(String[]::new);

        if (source instanceof GroupSource groupSource) {

            // メンションに反応する処理
            switch (args[0]) {
                case "予定" -> {
                    if (setupScheduleService.isStarted(groupSource.getGroupId())) {
                        reply(event.getReplyToken(), "既に予定を立てています。");
                        return defaultMessage;
                    };
                    setupScheduleService.start(groupSource.getGroupId(), lineMessagingClient.getGroupMembersIds(groupSource.getGroupId(), null).get().getMemberIds());
                    reply(event.getReplyToken(), "予定を立てました。");
                    reply(event.getReplyToken(), "活動場所を教えてください。@bot 場所 [場所]");
                    reply(event.getReplyToken(), "例: @bot 場所 渋谷");
                }
                case "場所" -> {
                    if (!setupScheduleService.isStarted(groupSource.getGroupId())) {
                        reply(event.getReplyToken(), "予定を立てていません。");
                        return defaultMessage;
                    }
                    reply(event.getReplyToken(), "場所を設定しました。");
                    setupScheduleService.setLocation(groupSource.getGroupId(), args[1]);
                }
                case "調査" -> {
                    if (!setupScheduleService.isStarted(groupSource.getGroupId())) {
                        reply(event.getReplyToken(), "予定を立てていません。");
                        return defaultMessage;
                    }
                    var session = setupScheduleService.getSession(groupSource.getGroupId());
                    if (session.getLocation() == null) {
                        reply(event.getReplyToken(), "場所を設定していません。");
                        return defaultMessage;
                    }
                    var category = service.pickUp(args[1]);
                    reply(event.getReplyToken(), "「" + session.getLocation() + "」周辺の" + "「" + category + "」"  + "を調査します。");
                    reply(event.getReplyToken(), Arrays.stream(googleMapsService.getShopInfo(session.getLocation(), category)).map(place -> place.name).collect(Collectors.joining("\n")));
                }
                case "追加" -> {
                    if (!setupScheduleService.isStarted(groupSource.getGroupId())) {
                        reply(event.getReplyToken(), "予定を立てていません。");
                        return defaultMessage;
                    }
                    var session = setupScheduleService.getSession(groupSource.getGroupId());
                    session.getActions();

                }
                case "確定" -> {

                }
                case "編集" -> {

                }
                default -> {
                    reply(event.getReplyToken(), """
                            使い方:
                            @bot 予定
                            @bot 確定
                            @bot 編集
                            @bot 調査
                            @bot 追加
                            """);
                }
            }

            var message = "メンションありがとうございます！";
            var replyMessage = new ReplyMessage(event.getReplyToken(), TextMessage.builder().text(message).build());
            lineMessagingClient.replyMessage(replyMessage);

        }
        return defaultMessage;
    }

    private void reply(String token, String message) {
        var replyMessage = new ReplyMessage(token, TextMessage.builder().text(message).build());
        lineMessagingClient.replyMessage(replyMessage);
    }
}

