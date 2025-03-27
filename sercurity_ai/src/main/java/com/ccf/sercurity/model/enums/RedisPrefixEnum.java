package com.ccf.sercurity.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisPrefixEnum {
    REGISTER_CODE("register_code_", 60 * 10),
    UPDATE_PASSWORD_CODE("update_password_code_", 60 * 10), // 10min
    WEAK_PASSWORD("weak_password", 60 * 60 * 24), // 1day
    ;
    private final String prefix;
    private final long expireTime;
}