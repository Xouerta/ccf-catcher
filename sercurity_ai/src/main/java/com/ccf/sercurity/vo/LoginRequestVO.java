package com.ccf.sercurity.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record LoginRequestVO(
        @NotNull
        @Email
        String email,
        @NotNull
        String password
) {
}
