package com.ccf.sercurity.controller;

import com.ccf.sercurity.annotation.Token;
import com.ccf.sercurity.model.TrafficData;
import com.ccf.sercurity.service.TrafficAnalysisService;
import com.ccf.sercurity.vo.PageResult;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/traffic")
public class TrafficController {

    private final TrafficAnalysisService trafficAnalysisService;

    @Validated
    @GetMapping(value = "/list")
    public ResponseEntity<PageResult<TrafficData>> listTraffic(
            @RequestHeader("Authorization") @Token String userId,
            @Min(1)
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @Min(10)
            @RequestParam(name = "size", defaultValue = "10") Integer size
    ) {

        return ResponseEntity.ok(trafficAnalysisService.listTraffic(userId, page, size));
    }

    @GetMapping("/analyze")
    public ResponseEntity analyze(
            @RequestHeader("Authorization") @Token String userId
    ) {
        return ResponseEntity.ok().build();
    }
}
