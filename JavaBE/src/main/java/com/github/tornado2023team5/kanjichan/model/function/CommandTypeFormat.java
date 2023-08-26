package com.github.tornado2023team5.kanjichan.model.function;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;
import lombok.Value;

@Value
public class CommandTypeFormat {
    @JsonPropertyDescription("コマンド。'MAKE_PLAN', 'SET_LOCATION', 'SEARCH_SPOTS', 'REMOVE_SPOT', 'ADOPT_SPOTS', 'MAKE_DRAFT', 'DECIDE_DRAFT', 'EDIT_AND_ADD_SPOT_FROM_DECIDED_DRAFT', 'EDIT_AND_REMOVE_SPOT_FROM_DECIDED_DRAFT', 'EDIT_AND_CHANGE_SPOT_FROM_DECIDED_DRAFT', 'NONE'のいずれか")
    public CommandType commandType;
}
