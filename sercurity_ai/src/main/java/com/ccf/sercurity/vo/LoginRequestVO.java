package com.ccf.sercurity.vo;

import jakarta.validation.constraints.NotNull;

public record LoginRequestVO(
        @NotNull
        String email,
        @NotNull
        String password
) {
}
