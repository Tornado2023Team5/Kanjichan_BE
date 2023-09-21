package com.github.tornado2023team5.kanjichan.controller;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@LineMessageHandler
@RequiredArgsConstructor
public class JoinController {
    private final LineMessagingClient lineMessagingClient;

    @EventMapping
    public TextMessage handleJoinEvent(JoinEvent event) {
        return new TextMessage("""
                                       はじめましてウサ！moonと言うウサ！
                                       こちらは友達との予定を円滑に決めることができるサービスウサ！！🥕
                                                                              
                                       まずはhttps://line.me/R/ti/p/%40207ingmp
                                       から、自分の予定を登録するウサ！🗓️
                                                                              
                                       使い方を知りたい時はメニューから「使い方」のボタンを押して欲しいウサ🐰
                                                                              
                                       🌕🥕Moonに挨拶しない人は予定に参加させてあげないウサよ！！🥕🌕
                                       """);
    }
}
