package com.ccf.sercurity.controller;

import com.ccf.sercurity.annotation.Token;
import com.ccf.sercurity.service.LogService;
import com.ccf.sercurity.vo.LogInfo;
import com.ccf.sercurity.vo.PageResult;
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
            @RequestParam(name = "size", defaultValue = "10") Integer size
    ) {

        return ResponseEntity.ok(logService.listLogs(userId, page, size));
    }

}
