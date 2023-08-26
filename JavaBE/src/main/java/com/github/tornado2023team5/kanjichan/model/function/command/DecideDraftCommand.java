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
public class DecideDraftCommand {
    @JsonPropertyDescription("複数の草案の中から選ぶときに使う。1から始まる")
    @JsonProperty(required = true)
    private int index;
}
