package com.github.tornado2023team5.kanjichan.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;
import lombok.Value;

@Data
public class ShopInfoSource {
    @JsonPropertyDescription("目的物のカテゴリー。焼肉、カフェ、水族館、温泉など")
    @JsonProperty(required = true)
    String value;
}
