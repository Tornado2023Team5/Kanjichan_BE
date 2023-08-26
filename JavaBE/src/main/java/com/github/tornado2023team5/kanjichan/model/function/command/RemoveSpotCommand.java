package com.github.tornado2023team5.kanjichan.model.function.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Value;

@Value
public class RemoveSpotCommand {
    @JsonPropertyDescription("候補から消すスポットのインデックス。情報がない無効な値の場合は-1。")
    @JsonProperty(required = true)
    public int index;
}
