package com.ccf.sercurity.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequestVO(
        @NotNull
        String username,
        @NotNull
        @Size(min = 6)
        String password,
        @NotNull
        @Email
        String email,
        @NotNull
        String code
) {
}
