package com.github.tornado2023team5.kanjichan.model.function.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public final class SetTimeCommand extends CommandModel {

    @JsonPropertyDescription("分。0から始まる。0分は0、1分は1、...、59分は59")
    @JsonProperty(required = true)
    private int minute;

    @JsonPropertyDescription("時間。0から始まる。0時は0、1時は1、...、23時は23")
    @JsonProperty(required = true)
    private int time;

    @JsonPropertyDescription("日にち。1から始まる。1日は1、2日は2、...、31日は31")
    @JsonProperty(required = true)
    private int day;

    @JsonPropertyDescription("月。1から始まる。1月は1、2月は2、...、12月は12")
    @JsonProperty(required = true)
    private int month;
}
