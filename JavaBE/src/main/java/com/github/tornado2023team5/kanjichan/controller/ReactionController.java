package com.github.tornado2023team5.kanjichan.controller;

import com.github.tornado2023team5.kanjichan.entity.Action;
import com.github.tornado2023team5.kanjichan.model.function.CommandInformationFormat;
import com.github.tornado2023team5.kanjichan.model.function.ShopCategory;
import com.github.tornado2023team5.kanjichan.model.function.command.MakePlanCommand;
import com.github.tornado2023team5.kanjichan.service.FunctionCallService;
import com.github.tornado2023team5.kanjichan.service.GoogleMapsService;
import com.github.tornado2023team5.kanjichan.service.SetupScheduleService;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@LineMessageHandler
@RestController
@RequiredArgsConstructor
public class ReactionController {
    private final LineMessagingClient lineMessagingClient;
    private final SetupScheduleService setupScheduleService;
    private final FunctionCallService functionCallService;
    private final GoogleMapsService googleMapsService;

//    @EventMapping
//    public void handleReaction(MessageEvent event) throws InterruptedException, IOException, ApiException {
//        Source source = event.getSource();
//        var message = event.getMessage();
//        if(!(message instanceof TextMessageContent textContent)) return null;
//        var messageText = textContent.getText();
//        StringBuilder reply = new StringBuilder();
//        if (!messageText.contains("@Moon")) return null;
//
//        String[] lines = messageText.split("\n");
//        String contentText = Arrays.stream(lines).skip(1).collect(Collectors.joining("\n"));
//        String[] args = Arrays.stream(lines[0].split(" ")).skip(1).toArray(String[]::new);
//
//        if (!(source instanceof GroupSource groupSource)) return null;
//
//        String id = groupSource.getGroupId();
//        CommandInformationFormat format = functionCallService.detect(messageText.replace("@Moon", ""), commandList(id));
//        return new TextMessage(reply.toString());
//    }
}

