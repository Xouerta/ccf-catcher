package com.ccf.sercurity.vo;

import java.util.Date;

public record UserInfoResponeVO(
        String id,
        String username,
        Date createdAt
) {
}
