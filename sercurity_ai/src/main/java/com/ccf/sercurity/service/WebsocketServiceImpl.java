package com.ccf.sercurity.service;

import com.ccf.sercurity.error.ErrorEnum;
import com.ccf.sercurity.error.PlatformException;
import com.ccf.sercurity.model.enums.WebsocketTypeEnum;
import com.ccf.sercurity.vo.WebsocketScannerVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class WebsocketServiceImpl implements WebsocketService {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void parseWebsocketEntity(WebsocketScannerVO<Object> websocketRequestVO) {
        WebsocketTypeEnum websocketTypeEnum = Optional.ofNullable(WebsocketTypeEnum.getEnum(websocketRequestVO.getType()))
                .orElseThrow(() -> new PlatformException(ErrorEnum.BAD_REQUEST));
    }

    public static <T> T getObject(Object object, Class<T> clazz) {
        return objectMapper.convertValue(object, clazz);
    }
}
