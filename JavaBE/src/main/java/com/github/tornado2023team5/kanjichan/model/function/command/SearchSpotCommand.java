package com.github.tornado2023team5.kanjichan.model.function.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchSpotCommand {
    @JsonPropertyDescription("目的物のカテゴリー。焼肉、カフェ、水族館、マックなど")
    @JsonProperty(required = true)
    private String category;
    @JsonPropertyDescription("集合場所。渋谷、新宿、池袋、秋葉原など")
    private String destination;
}
