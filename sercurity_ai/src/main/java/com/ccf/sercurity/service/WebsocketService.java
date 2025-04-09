package com.ccf.sercurity.service;

import com.ccf.sercurity.vo.WebsocketScannerVO;

public interface WebsocketService {
    void parseWebsocketEntity(WebsocketScannerVO<Object> websocketRequestVO);
}
