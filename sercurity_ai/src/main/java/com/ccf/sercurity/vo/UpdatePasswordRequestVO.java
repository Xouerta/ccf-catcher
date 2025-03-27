package com.ccf.sercurity.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record UpdatePasswordRequestVO(
        @NotNull
        @Email
        String email,
        @NotNull
        String newPassword,
        @NotNull
        String code
) {
}
