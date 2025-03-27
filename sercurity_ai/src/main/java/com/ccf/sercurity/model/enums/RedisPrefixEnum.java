package com.ccf.sercurity.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisPrefixEnum {
    REGISTER_CODE("_register_code", 60 * 10),
    UPDATE_PASSWORD_CODE("_update_password_code", 60 * 10), // 10min
    WEAK_PASSWORD("_weak_password", 60 * 60 * 24), // 1day
    ;
    private final String prefix;
    private final long expireTime;
}