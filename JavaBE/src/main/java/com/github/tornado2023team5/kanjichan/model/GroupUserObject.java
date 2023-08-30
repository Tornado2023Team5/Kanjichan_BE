package com.github.tornado2023team5.kanjichan.model;

import lombok.Value;

import java.util.List;

@Value
public class GroupUserObject {
    String groupId;
    List<String> userIds;
}
