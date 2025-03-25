/*
package com.ccf.sercurity.jwt;


import com.ccf.sercurity.error.ErrorEnum;
import com.ccf.sercurity.error.PlatformException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;


@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String token = getTokenFromHttpRequest(request);
        if (!StringUtils.hasText(token)) {
            throw new PlatformException(ErrorEnum.UNAUTHORIZED);
        }
        return true;
    }

    private @Nullable String getTokenFromHttpRequest(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(SecurityConstants.TOKEN_HEADER))
                .filter(header -> header.startsWith(SecurityConstants.TOKEN_PREFIX))
                .map(header -> header.replace(SecurityConstants.TOKEN_PREFIX, "")).orElse(null);
    }
}*/
