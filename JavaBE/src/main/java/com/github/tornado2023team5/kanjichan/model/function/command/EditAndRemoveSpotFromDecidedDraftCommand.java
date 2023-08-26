package com.github.tornado2023team5.kanjichan.model.function.command;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Value;

@Value
public class EditAndRemoveSpotFromDecidedDraftCommand {
    @JsonPropertyDescription("削除するスポットを追加する位置")
    public int index;
}
