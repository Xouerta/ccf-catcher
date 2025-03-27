package com.ccf.sercurity.controller;

import com.ccf.sercurity.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/logs")
public class LogsController {
    private final LogService logService;

}
