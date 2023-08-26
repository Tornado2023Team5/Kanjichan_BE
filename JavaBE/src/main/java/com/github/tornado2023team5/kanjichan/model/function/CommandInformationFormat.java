package com.github.tornado2023team5.kanjichan.model.function;

import lombok.Data;
import lombok.Value;

@Value
public class CommandInformationFormat {
    public CommandType commandType;
    public String information;
}
