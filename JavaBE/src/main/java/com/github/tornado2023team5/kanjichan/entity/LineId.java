package com.github.tornado2023team5.kanjichan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LineId {
    private List<String> lineIds;
}
