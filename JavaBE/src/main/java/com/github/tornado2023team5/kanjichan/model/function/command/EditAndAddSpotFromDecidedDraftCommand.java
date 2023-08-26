package com.github.tornado2023team5.kanjichan.model.function.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Value;

@Value
public class EditAndAddSpotFromDecidedDraftCommand {
    @JsonPropertyDescription("新しく追加するスポットを追加する位置。情報がない無効な値の場合は-1。")
    @JsonProperty(required = true)
    public int index;
    @JsonPropertyDescription("新しく追加するスポットの名前")
    public String name;
    @JsonPropertyDescription("新しく追加するスポットのURL")
    public String url;
}
