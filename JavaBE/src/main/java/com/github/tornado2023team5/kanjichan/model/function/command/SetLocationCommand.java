package com.github.tornado2023team5.kanjichan.model.function.command;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Value;

@Value
public class SetLocationCommand {
    @JsonPropertyDescription("目的地。活動ばよ。渋谷、新宿、池袋、秋葉原など")
    public String destination;
}
