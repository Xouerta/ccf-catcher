package com.ccf.sercurity.controller;

import com.ccf.sercurity.annotation.Token;
import com.ccf.sercurity.model.FileInfo;
import com.ccf.sercurity.service.FileService;
import com.ccf.sercurity.service.MaliciousDetectionService;
import com.ccf.sercurity.vo.PageResult;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@Validated
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

    @GetMapping(value = "/list")
    public ResponseEntity<PageResult<?>> listFiles(
            @RequestHeader("Authorization") @Token String userId,
            @Min(1)
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {

        return ResponseEntity.ok(fileService.listFiles(userId, page, size));
    }


} 