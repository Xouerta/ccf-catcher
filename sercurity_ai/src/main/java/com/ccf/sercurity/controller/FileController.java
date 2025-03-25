package com.ccf.sercurity.controller;

import com.ccf.sercurity.annotation.Token;
import com.ccf.sercurity.model.FileInfo;
import com.ccf.sercurity.service.FileService;
import com.ccf.sercurity.service.MaliciousDetectionService;
import com.github.xingfudeshi.knife4j.annotations.ApiOperationSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/files")
public class FileController {
    private final FileService fileService;
    private final MaliciousDetectionService maliciousDetectionService;

    @Autowired
    public FileController(FileService fileService, MaliciousDetectionService maliciousDetectionService) {
        this.fileService = fileService;
        this.maliciousDetectionService = maliciousDetectionService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestHeader("Authorization") @Token String userId) {
        try {
            // 保存文件信息
            FileInfo fileInfo = fileService.saveFile(file, userId);
            
            // 检测文件是否恶意
            boolean isMalicious = maliciousDetectionService.detectMaliciousFile(fileInfo, file);
            
            if (isMalicious) {
                return ResponseEntity.badRequest().body(Map.of("msg", "文件上传失败: 文件被检测为恶意文件", "code", 1043));
            }
            
            return ResponseEntity.ok(fileInfo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("文件上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileInfo> getFileInfo(@PathVariable String id) {
        FileInfo fileInfo = fileService.getFileById(id);
        return ResponseEntity.ok(fileInfo);
    }

} 