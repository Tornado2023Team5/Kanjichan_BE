package com.github.tornado2023team5.kanjichan.model.function;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommandTypeFormat {
    @JsonPropertyDescription("ユーザー入力の元呼び出すコマンド。'MAKE_PLAN', 'SET_LOCATION', 'SEARCH_SPOTS', 'REMOVE_SPOT', 'ADOPT_SPOTS', 'MAKE_DRAFT', 'DECIDE_DRAFT', 'EDIT_AND_ADD_SPOT_FROM_DECIDED_DRAFT', 'EDIT_AND_REMOVE_SPOT_FROM_DECIDED_DRAFT', 'EDIT_AND_CHANGE_SPOT_FROM_DECIDED_DRAFT', 'NONE' のいずれか")
    @JsonProperty(required = true)
    private CommandType commandType;
}
