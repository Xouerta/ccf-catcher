package com.ccf.sercurity.service;

import com.ccf.sercurity.config.DeepStudyLogConfig;
import com.ccf.sercurity.model.DeepStudyLog;
import com.ccf.sercurity.model.enums.WebsocketTypeEnum;
import com.ccf.sercurity.repository.DeepStudyLogRepository;
import com.ccf.sercurity.vo.DeepStudyModelResponeVO;
import com.ccf.sercurity.vo.PageResult;
import com.ccf.sercurity.vo.WebsocketPushVO;
import com.ccf.sercurity.websocket.WebSocketServer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeepStudyLogService {
    private final DeepStudyLogConfig deepStudyLogConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RestTemplate restTemplate = new RestTemplate();

    private final DeepStudyLogRepository deepStudyLogRepository;

    private static DeepStudyModelResponeVO deepStudyModelResponeVO;

    public void saveDeepStudyLog(String message) throws JsonProcessingException {
        DeepStudyLog deepStudyLog = objectMapper.readValue(message, DeepStudyLog.class);
        deepStudyLog.setTimestamp(new Date());

        Pattern pattern = Pattern.compile("^(?<timestamp>[^ ]+) (?<hostname>\\S+) (?<message>.+)$");
        Matcher matcher = pattern.matcher(deepStudyLog.getLogText());
        if (matcher.find()) {
            deepStudyLog.setLogMessage(matcher.group("message"));
            deepStudyLog.setHost(matcher.group("hostname"));
        }
        deepStudyLogRepository.save(deepStudyLog);

        WebsocketPushVO<Object> pushVO = new WebsocketPushVO<>()
                .setType(WebsocketTypeEnum.DEEP_STUDY_LOG.getType())
                .setCode(HttpStatus.OK.value())
                .setData(deepStudyLog);
        WebSocketServer.sendMessage(pushVO);
        log.info("websocket推送消息: {}", pushVO);
    }

    public DeepStudyModelResponeVO getDeepStudyModelResponeVO(String userId) {
        log.info("用户 {} 查询模型信息", userId);
        return deepStudyModelResponeVO;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void handleContextRefreshed() {
        Map<String, Object> requestBody = Map.of("labeled_log_file", "auth.labeled.log", "threshold", 0.8614609496866266);
        ResponseEntity<DeepStudyModelResponeVO> vo = restTemplate.postForEntity(deepStudyLogConfig.getEvaluateUrl(), requestBody, DeepStudyModelResponeVO.class);

        deepStudyModelResponeVO = vo.getBody();
        log.info("deepStudyModelResponseVO: {}", deepStudyModelResponeVO);
    }

    public PageResult<DeepStudyLog> list(String userId, @Min(1) Integer page, @Min(10) Integer size, Boolean attack, String host) {

        PageRequest pageRequest = PageRequest.of(page - 1, size);
        log.info("用户 {} 请求查看日志 page: {} size: {} attack: {} host: {}", userId, page, size, attack, host);
        Page<DeepStudyLog> pages;

        if (attack != null && host != null) {
            pages = deepStudyLogRepository.findByAttackDetectedAndHost(attack, host, pageRequest);
        } else if (attack != null) {
            pages = deepStudyLogRepository.findByAttackDetected(attack, pageRequest);
        } else if (host != null) {
            pages = deepStudyLogRepository.findByHost(host, pageRequest);
        } else {
            pages = deepStudyLogRepository.findBy(pageRequest);
        }


        PageResult<DeepStudyLog> pageResult = new PageResult<>();
        pageResult.setTotal(pages.getTotalElements());
        pageResult.setPage(pages.getNumber() + 1);
        pageResult.setSize(pages.getSize());
        pageResult.setList(pages.getContent());
        return pageResult;
    }
}
