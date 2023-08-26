package com.github.tornado2023team5.kanjichan.model.function.command;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Value;

@Value
public class SearchSpotCommand {
    @JsonPropertyDescription("目的物のカテゴリー。焼肉、カフェ、水族館、マックなど")
    public String category;
}
