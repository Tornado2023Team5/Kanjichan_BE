package com.github.tornado2023team5.kanjichan.model.function.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public final class RemoveSpotCommand extends CommandModel {
    @JsonPropertyDescription("候補から消すスポット")
    @JsonProperty(required = true)
    private List<String> spots;
}
