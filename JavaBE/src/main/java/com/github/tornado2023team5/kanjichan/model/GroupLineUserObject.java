package com.github.tornado2023team5.kanjichan.model;

import lombok.Value;

import java.util.List;

@Value
public class GroupLineUserObject {
    String groupId;
    List<String> lineUserIds;
}
