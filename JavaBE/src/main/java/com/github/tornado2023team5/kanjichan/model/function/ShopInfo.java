package com.github.tornado2023team5.kanjichan.model.function;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Value;

@Value
public class ShopInfo {
    String value;
    float rate;
}
