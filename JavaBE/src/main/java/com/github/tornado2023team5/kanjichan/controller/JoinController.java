package com.github.tornado2023team5.kanjichan.controller;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import lombok.RequiredArgsConstructor;

@LineMessageHandler
@RequiredArgsConstructor
public class JoinController {
    private final LineMessagingClient lineMessagingClient;

    @EventMapping
    public TextMessage handleJoinEvent(JoinEvent event) {
        return new TextMessage("""
                                       はじめましてウサ！moonと言うウサ！
                                       こちらは友達との予定を円滑に決めることができるサービスウサ！！🥕
                                                                              
                                       まずはhttps://liff.line.me/2000328519-0KKJlMZ7
                                       から、自分の予定を登録するウサ！🗓️
                                       https://moon-usa.vercel.app/auth?openExternalBrowser=1
                                       からgoogleカレンダーと連携して自動で予定を反映することもできるウサよ！🌕
                                                                              
                                       使い方を知りたい時はメニューから「使い方」のボタンを押して欲しいウサ🐰
                                       """);
    }
}