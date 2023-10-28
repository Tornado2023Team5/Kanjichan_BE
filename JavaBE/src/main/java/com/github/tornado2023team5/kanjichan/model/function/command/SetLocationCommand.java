package com.github.tornado2023team5.kanjichan.model.function.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public final class SetLocationCommand extends CommandModel {
    @JsonPropertyDescription("目的地。活動ばよ。渋谷、新宿、池袋、秋葉原など")
    @JsonProperty(required = true)
    private String destination;
}
