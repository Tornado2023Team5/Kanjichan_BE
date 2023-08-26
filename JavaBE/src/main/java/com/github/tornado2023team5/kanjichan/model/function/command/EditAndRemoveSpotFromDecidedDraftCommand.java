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
public class EditAndRemoveSpotFromDecidedDraftCommand {
    @JsonPropertyDescription("削除するスポットを追加する位置")
    @JsonProperty(required = true)
    private int index;
}
