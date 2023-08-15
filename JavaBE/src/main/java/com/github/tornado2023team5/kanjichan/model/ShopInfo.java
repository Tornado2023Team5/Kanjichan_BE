package com.github.tornado2023team5.kanjichan.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Value;

@Value
public class ShopInfo {
    @JsonPropertyDescription("お店の名前")
    String name;

    @JsonPropertyDescription("お店のURL")
    String url;

//    @JsonPropertyDescription("お店の場所")
//    String location;
}