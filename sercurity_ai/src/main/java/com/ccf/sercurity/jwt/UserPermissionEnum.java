package com.ccf.sercurity.jwt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserPermissionEnum {
    COMMON(0, "common"),
    ADMIN(5, "admin");
    private final Integer permission;
    private final String name;

    public static UserPermissionEnum userPermissionByPermission(Integer permission) {
        for (UserPermissionEnum userPermission : UserPermissionEnum.values()) {
            if(permission.equals(userPermission.getPermission())){
                return userPermission;
            }
        }
        return null;
    }
}
