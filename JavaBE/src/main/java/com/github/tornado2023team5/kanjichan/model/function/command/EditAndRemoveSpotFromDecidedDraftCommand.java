package com.github.tornado2023team5.kanjichan.model.function.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public final class EditAndRemoveSpotFromDecidedDraftCommand extends CommandModel {
    @JsonPropertyDescription("削除するスポットを追加する位置")
    @JsonProperty(required = true)
    private List<String> spots;
}
