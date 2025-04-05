package com.ccf.sercurity.controller;

import com.ccf.sercurity.annotation.Token;
import com.ccf.sercurity.service.LogService;
import com.ccf.sercurity.vo.LogInfo;
import com.ccf.sercurity.vo.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/logs")
public class LogsController {
    private final LogService logService;

    @Validated
    @GetMapping(value = "/list")
    public ResponseEntity<PageResult<LogInfo>> listLogs(
            @RequestHeader("Authorization") @Token String userId,
            @Min(1)
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @Min(10)
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @Schema(description = "network_error (连不上deepseek)、 INFO、 ERROR")
            @RequestParam(name = "status", required = false) String status,
            @Schema(description = "主机名")
            @RequestParam(name = "host", required = false) String host)
    {

        return ResponseEntity.ok(logService.listLogs(userId, page, size, status, host));
    }

}
