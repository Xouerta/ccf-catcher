package com.ccf.sercurity.jwt;

public class SecurityConstants {
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    public static final long EXPIRATION_REMEMBER_TIME = 365 * 30 * 24 * 60 * 60L; // 秒
    public static final long EXPIRATION_TIME = 30 * 24 * 60 * 60L; // 秒
    public static final String TOKEN_ROLE_CLAIM = "roles";
    public static final String TOKEN_ISSUER = "security";
}
