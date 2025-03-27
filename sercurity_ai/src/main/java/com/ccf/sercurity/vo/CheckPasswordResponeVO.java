package com.ccf.sercurity.vo;

import io.swagger.v3.oas.annotations.media.Schema;

public record CheckPasswordResponeVO(
        @Schema(description = "是否弱密码")
        Boolean isWeak
) {
}
