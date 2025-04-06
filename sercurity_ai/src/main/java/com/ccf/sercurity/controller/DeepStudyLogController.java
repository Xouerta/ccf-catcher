package com.ccf.sercurity.controller;

import com.ccf.sercurity.annotation.Token;
import com.ccf.sercurity.model.DeepStudyLog;
import com.ccf.sercurity.service.DeepStudyLogService;
import com.ccf.sercurity.vo.DeepStudyModelResponeVO;
import com.ccf.sercurity.vo.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/deepStudyLog")
public class DeepStudyLogController {

    private final DeepStudyLogService deepStudyLogService;

    @GetMapping("/getDeepStudyModelResponseVO")
    public ResponseEntity<DeepStudyModelResponeVO> getDeepStudyModelResponseVO(
            @RequestHeader("Authorization") @Token String userId
    ) {
        return ResponseEntity.ok(deepStudyLogService.getDeepStudyModelResponeVO(userId));
    }

    @Validated
    @GetMapping("/list")
    public ResponseEntity<PageResult<DeepStudyLog>> list(
            @RequestHeader("Authorization") @Token String userId,
            @Min(1)
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @Min(10)
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @Schema(name = "是否检测到攻击")
            @RequestParam(name = "attack", required = false) boolean attack,
            @Schema(name = "主机")
            @RequestParam(name = "host", required = false) String host
    ) {
        return ResponseEntity.ok(deepStudyLogService.list(userId, page, size, attack, host));
    }
}
