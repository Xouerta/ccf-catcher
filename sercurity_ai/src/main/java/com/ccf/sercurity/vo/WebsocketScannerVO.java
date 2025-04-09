package com.ccf.sercurity.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class WebsocketScannerVO<T> {
    private T data;
    private String type;

}
