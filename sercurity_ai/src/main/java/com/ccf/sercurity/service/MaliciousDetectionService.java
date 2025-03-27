package com.ccf.sercurity.service;

import com.ccf.sercurity.model.FileInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.Map;

/**
 * 恶意文件检测服务类
 * 负责调用外部API检测上传文件是否为恶意文件
 */
@Slf4j
@Service
public class MaliciousDetectionService {

    private enum FileSecurity {
        SAFE,
        MALICIOUS
    }

    /**
     * 恶意文件检测API的URL
     */
    @Value("${malicious.detection.api.url}")
    private String detectionApiUrl;

    /**
     * HTTP客户端
     */
    private final RestTemplate restTemplate;

    private final FileService fileService;

    /**
     * 构造函数，初始化RestTemplate
     */

    public MaliciousDetectionService(FileService fileService) {
        this.restTemplate = new RestTemplate();
        this.fileService = fileService;
    }

    /**
     * 检测文件是否为恶意文件
     *
     * @param fileInfo 文件信息对象
     * @return 如果文件被检测为恶意文件返回true，否则返回false
     */
    public boolean detectMaliciousFile(FileInfo fileInfo, MultipartFile file) {
        try {
            // 准备请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // 准备请求体，包含文件
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            body.add("file", file.getResource());

            // 创建请求实体
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            log.info("用户 {} 发送文件到恶意检测", fileInfo.getUploadFileUserId());

            // 发送请求到恶意检测API
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    detectionApiUrl,
                    requestEntity,
                    Map.class);

            boolean isMalicious = false;
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && "success".equals(responseBody.get("status"))) {
                // 更新文件检测结果
                isMalicious = FileSecurity.MALICIOUS.name().equals(responseBody.get("prediction"));
                fileInfo.setMalicious(isMalicious);
                log.info("用户 {} 文件 {} 检测结果: {}", fileInfo.getUploadFileUserId(), fileInfo.getOriginalName(), isMalicious ? "恶意" : "安全");

                if (responseBody.containsKey("confidence")) {
                    fileInfo.setConfidence(Double.parseDouble(responseBody.get("confidence").toString()));
                    log.info("用户 {} 文件 {} 置信度: {}", fileInfo.getUploadFileUserId(), fileInfo.getOriginalName(), fileInfo.getConfidence());
                }
                fileInfo.setDetectionTime(new Date());
                fileService.updateFileInfoById(fileInfo);
            } else {
                // 日志记录检测失败
                log.error("恶意文件检测失败: {}", responseBody.get("message"));
                System.err.println("恶意文件检测失败: " + responseBody.get("message"));
            }

            return isMalicious;
        } catch (Exception e) {
            // 日志记录异常
            log.error("恶意文件检测失败: {}", e.getMessage());
            System.err.println("恶意文件检测失败: " + e.getMessage());
            return false;
        }
    }

} 