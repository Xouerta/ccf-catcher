package com.ccf.sercurity.vo;

import jakarta.validation.constraints.NotNull;

public record RegisterRequestVO(
        @NotNull
        String username,
        @NotNull
        String password,
        @NotNull
        String email
) {
}
