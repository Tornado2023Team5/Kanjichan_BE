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
    @JsonPropertyDescription("""
            ユーザー入力の元呼び出すコマンド。
            
            'JOIN_PLAN',
            'MAKE_PLAN',
            'RESET_PLAN'
            'CONFIRM_PLAN',
            'SET_LOCATION',
            'SEARCH_SPOTS',
            'SHOW_ADOPTED_SPOTS',
            'NONE'
            のいずれか"
            """)
    @JsonProperty(required = true)
    private CommandType commandType;
}
