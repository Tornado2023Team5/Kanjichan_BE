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
public class EditAndChangeSpotFromDecidedDraftCommand {
    @JsonPropertyDescription("順番を入れ替えるスポットの位置")
    @JsonProperty(required = true)
    private int fromIndex;
    @JsonPropertyDescription("順番を入れ替えた後のスポットの位置")
    @JsonProperty(required = true)
    private int toIndex;
}
