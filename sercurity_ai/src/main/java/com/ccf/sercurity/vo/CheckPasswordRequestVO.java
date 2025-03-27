package com.ccf.sercurity.vo;

import jakarta.validation.constraints.NotNull;

public record CheckPasswordRequestVO(
        @NotNull
        String password
) {
}
