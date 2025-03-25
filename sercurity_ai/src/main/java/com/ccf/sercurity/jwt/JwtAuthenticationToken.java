package com.ccf.sercurity.jwt;

import lombok.ToString;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.List;

@ToString(callSuper = true)
public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private final String principal;

    public JwtAuthenticationToken(String id) {
        super(List.of());
        this.principal = id;
        this.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }
}
