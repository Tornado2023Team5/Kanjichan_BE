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
public class SetLocationCommand {
    @JsonPropertyDescription("目的地。活動ばよ。渋谷、新宿、池袋、秋葉原など")
    @JsonProperty(required = true)
    private String destination;
}
