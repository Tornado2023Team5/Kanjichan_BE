package com.github.tornado2023team5.kanjichan.model.function.command;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MakePlanCommand {
    @JsonPropertyDescription("目的物のカテゴリー。焼肉、カフェ、水族館、温泉など")
    private String category;
    @JsonPropertyDescription("集合場所。渋谷、新宿、池袋、秋葉原など")
    private String destination;
    @JsonPropertyDescription("説明。詳細。9時から12時まで、途中で山田さんと合流など")
    private String description;
}
