package com.github.tornado2023team5.kanjichan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LineProfile {
    private String id;

    private String lineUserId;  // この属性には@UniqueConstraintを使用して一意性を保証することができます。

    private String pictureUrl;

    private String lineUserName;

    private User user;
}