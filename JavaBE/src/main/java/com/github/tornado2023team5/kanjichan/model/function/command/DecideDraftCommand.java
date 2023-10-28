package com.github.tornado2023team5.kanjichan.model.function.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public final class DecideDraftCommand extends CommandModel {
    @JsonPropertyDescription("複数の草案の中から選ぶときに使う。1から始まる")
    @JsonProperty(required = true)
    private int index;
}
