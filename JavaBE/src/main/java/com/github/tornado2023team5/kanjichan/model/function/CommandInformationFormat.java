package com.github.tornado2023team5.kanjichan.model.function;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;

@Data
@AllArgsConstructor
public class CommandInformationFormat {
    private CommandType commandType;
    private String information;
}
