package com.github.tornado2023team5.kanjichan.controller;

import com.github.tornado2023team5.kanjichan.service.LineMessageService;
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

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@LineMessageHandler
@Component
@RequiredArgsConstructor
public class MentionController {
    private final LineMessagingClient lineMessagingClient;
    private final LineMessageService lineMessageService;

    @EventMapping
    public TextMessage handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws ExecutionException, InterruptedException {
        Source source = event.getSource();
        var messageText = event.getMessage().getText();
        var defaultMessage = new TextMessage(messageText);
        if (!messageText.contains("@" + messageText)) return defaultMessage;
        String[] lines = messageText.split("\n");
        if(lines.length <= 1) {
            reply(event.getReplyToken(), "メンションありがとうございます！");
            return defaultMessage;
        }

        String contentText = Arrays.stream(lines).skip(1).collect(Collectors.joining("\n"));
        String[] args = Arrays.stream(lines[0].split(" ")).skip(1).toArray(String[]::new);

        if (source instanceof GroupSource groupSource) {
            String botUserId = lineMessagingClient.getBotInfo().get().getUserId();

            // メンションに反応する処理
            switch (args[0]) {
                case "予定" -> {

                }
                case "確定" -> {

                }
                case "編集" -> {

                }
                case "調査" -> {

                }
                case "追加" -> {

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

