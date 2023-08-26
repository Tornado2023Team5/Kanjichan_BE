package com.github.tornado2023team5.kanjichan.model.function.command;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Value;

@Value
public class DecideDraftCommand {
    @JsonPropertyDescription("複数の草案の中から選ぶときに使う。1から始まる")
    public int index;
}
