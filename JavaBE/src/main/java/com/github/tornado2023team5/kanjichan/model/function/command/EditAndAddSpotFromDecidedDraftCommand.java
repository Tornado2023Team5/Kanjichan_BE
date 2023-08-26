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
public class EditAndAddSpotFromDecidedDraftCommand {
    @JsonPropertyDescription("新しく追加するスポットを追加する位置。情報がない無効な値の場合は-1。")
    @JsonProperty(required = true)
    private int index;
    @JsonPropertyDescription("新しく追加するスポットの名前")
    private String name;
    @JsonPropertyDescription("新しく追加するスポットのURL")
    private String url;
}
