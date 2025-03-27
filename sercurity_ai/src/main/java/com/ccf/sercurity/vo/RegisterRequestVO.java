package com.ccf.sercurity.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record RegisterRequestVO(
        @NotNull
        String username,
        @NotNull
        String password,
        @NotNull
        @Email
        String email,
        @NotNull
        String code
) {
}
