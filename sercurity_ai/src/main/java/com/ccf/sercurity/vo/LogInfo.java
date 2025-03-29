package com.ccf.sercurity.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

public record LogInfo(
        String id,
        @Schema(description = "主机名")
        String host,
        @Schema(description = "产生本条日志的文件名")
        String logFileName,
        @Schema(description = "日志信息")
        String message,
        @Schema(description = "网络错误、 info、 error")
        String status,
        @Schema(description = "时间戳")
        Date timestamp
) {
}
