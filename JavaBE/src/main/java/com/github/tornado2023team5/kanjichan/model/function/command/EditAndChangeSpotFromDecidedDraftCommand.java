package com.github.tornado2023team5.kanjichan.model.function.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public final class EditAndChangeSpotFromDecidedDraftCommand extends CommandModel {
    @JsonPropertyDescription("順番を入れ替えるスポットの位置")
    @JsonProperty(required = true)
    private int fromIndex;
    @JsonPropertyDescription("順番を入れ替えた後のスポットの位置")
    @JsonProperty(required = true)
    private int toIndex;
}
