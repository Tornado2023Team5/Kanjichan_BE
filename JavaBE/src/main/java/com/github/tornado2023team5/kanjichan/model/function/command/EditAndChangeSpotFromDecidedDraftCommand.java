package com.github.tornado2023team5.kanjichan.model.function.command;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Value;

@Value
public class EditAndChangeSpotFromDecidedDraftCommand {
    @JsonPropertyDescription("順番を入れ替えるスポットの位置")
    public int fromIndex;
    @JsonPropertyDescription("順番を入れ替えた後のスポットの位置")
    public int toIndex;
}
