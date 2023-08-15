package com.github.tornado2023team5.kanjichan.configuration;

import com.linecorp.bot.client.LineMessagingClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LineBotConfig {
    private final String channelAccessToken = "YOUR_CHANNEL_ACCESS_TOKEN";

    @Bean
    public LineMessagingClient lineMessagingClient() {
        return LineMessagingClient.builder(channelAccessToken).build();
    }
}
