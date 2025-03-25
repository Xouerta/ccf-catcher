package com.ccf.sercurity.error;

public class PlatformException extends RuntimeException {

    private final ErrorEnum errorEnum;

    public PlatformException(ErrorEnum errorEnum) {
        super(errorEnum.getMsg());
        this.errorEnum = errorEnum;
    }

    public int getCode() {
        return errorEnum.getCode();
    }

    public String getMsg() {
        return errorEnum.getMsg();
    }

}
