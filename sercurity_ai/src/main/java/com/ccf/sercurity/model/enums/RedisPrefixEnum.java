package com.ccf.sercurity.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisPrefixEnum {
    REGISTER_CODE("_register_code"),
    UPDATE_PASSWORD_CODE("_update_password_code"),;
    private final String prefix;
}