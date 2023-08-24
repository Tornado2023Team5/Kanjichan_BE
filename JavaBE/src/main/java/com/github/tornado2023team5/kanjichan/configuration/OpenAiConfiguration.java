package com.github.tornado2023team5.kanjichan.configuration;

import com.theokanning.openai.service.OpenAiService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfiguration {
    private static final String openAiToken = System.getenv("OPENAI_API_KEY");

    @Bean
    public OpenAiService openAiService() {
        return new OpenAiService(openAiToken);
    }
}
