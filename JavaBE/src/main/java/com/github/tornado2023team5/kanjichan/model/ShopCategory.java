package com.github.tornado2023team5.kanjichan.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Value;

@Value
public class ShopCategory {
    @JsonProperty(required = true)
    @JsonPropertyDescription("目的地、目的物のカテゴリ。例: お肉、水族館、カフェなど")
    String name;
}
