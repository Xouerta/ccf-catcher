package com.ccf.sercurity.vo;

import com.ccf.sercurity.model.enums.WebsocketTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Data
@Accessors(chain = true)
public class WebsocketPushVO<T> {
    private Integer code;
    private T data;
    private String type;

    public static <T> WebsocketPushVO<T> ok(T value, WebsocketTypeEnum type) {
        return new WebsocketPushVO<T>().setCode(HttpStatus.OK.value()).setData(value).setType(type.getType());
    }
}
