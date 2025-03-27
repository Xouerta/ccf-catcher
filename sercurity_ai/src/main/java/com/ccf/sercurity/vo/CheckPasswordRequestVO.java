package com.ccf.sercurity.vo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CheckPasswordRequestVO(
        @NotNull
        @Size(min = 6)
        String password
) {
}
