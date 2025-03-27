package com.ccf.sercurity.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenRequestMappingHandlerAdapter {
    private final RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    @PostConstruct
    public void init() {
        List<HandlerMethodArgumentResolver> argumentResolvers = requestMappingHandlerAdapter.getArgumentResolvers();

        assert argumentResolvers != null;

        ArrayList<HandlerMethodArgumentResolver> handlerMethodArgumentResolvers = new ArrayList<>(argumentResolvers);

        handlerMethodArgumentResolvers.addFirst(new TokenMethodArgumentResolver());
        requestMappingHandlerAdapter.setArgumentResolvers(handlerMethodArgumentResolvers);
    }


}
