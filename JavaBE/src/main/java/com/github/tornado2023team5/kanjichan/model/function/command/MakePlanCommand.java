package com.github.tornado2023team5.kanjichan.model.function.command;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;
import lombok.Value;

@Value
public class MakePlanCommand {
    @JsonPropertyDescription("目的物のカテゴリー。焼肉、カフェ、水族館、温泉など")
    public String category;
    @JsonPropertyDescription("目的地。活動ばよ。渋谷、新宿、池袋、秋葉原など")
    public String destination;
}
