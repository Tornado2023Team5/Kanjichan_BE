package com.github.tornado2023team5.kanjichan.configuration;

import com.theokanning.openai.service.OpenAiService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfig {
    private final String openAiToken = "YOUR_CHANNEL_ACCESS_TOKEN";

    @Bean
    public OpenAiService openAiService() {
        return new OpenAiService(openAiToken);
    }
}
