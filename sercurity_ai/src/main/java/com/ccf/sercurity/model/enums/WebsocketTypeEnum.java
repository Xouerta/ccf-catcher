package com.ccf.sercurity.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum WebsocketTypeEnum {
    DEEP_STUDY_LOG("deep_study_log"),
    TRAFFIC("traffic"),
    FILE("file"),
    ERROR("error"),
    ;
    private final String type;

    public static WebsocketTypeEnum getEnum(String type) {
        if (Arrays.stream(WebsocketTypeEnum.values()).noneMatch(x -> x.getType().equals(type))) {
            return null;
        }
        return Arrays.stream(WebsocketTypeEnum.values())
                .filter(x -> x.getType().equals(type))
                .toList()
                .getFirst();
    }
}
