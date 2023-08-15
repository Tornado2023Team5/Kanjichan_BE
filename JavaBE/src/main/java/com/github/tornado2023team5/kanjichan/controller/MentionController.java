package com.github.tornado2023team5.kanjichan.controller;

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

import java.util.concurrent.ExecutionException;

@LineMessageHandler
@Component
@RequiredArgsConstructor
public class MentionController {
    private final LineMessagingClient lineMessagingClient;

    @EventMapping
    public TextMessage handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws ExecutionException, InterruptedException {
        Source source = event.getSource();
        if (source instanceof GroupSource groupSource) {
            String botUserId = lineMessagingClient.getBotInfo().get().getUserId();

            var messageText = event.getMessage().getText();
            if (messageText.contains("@" + botUserId)) {
                // メンションに反応する処理
                var message = "メンションありがとうございます！";
                var replyMessage = new ReplyMessage(event.getReplyToken(), TextMessage.builder().text(message).build());
                lineMessagingClient.replyMessage(replyMessage);
            }
        }
        return new TextMessage(event.getMessage().getText());
    }
}

