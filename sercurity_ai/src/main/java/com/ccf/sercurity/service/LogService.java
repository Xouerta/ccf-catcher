package com.ccf.sercurity.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.ccf.sercurity.config.LogConfig;
import com.ccf.sercurity.model.LogRecord;
import com.ccf.sercurity.repository.LogRepository;
import com.ccf.sercurity.vo.AnalysisLogResultVO;
import com.ccf.sercurity.vo.LogInfo;
import com.ccf.sercurity.vo.PageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日志服务类
 * 负责记录和查询系统日志
 */
@Slf4j
@Service
public class LogService {
    /**
     * 日志记录仓库接口
     */
    private final LogRepository logRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RestTemplate restTemplate;

    private final LogConfig logConfig;

    private final ElasticsearchClient client;
    /**
     * 构造函数，注入日志仓库
     *
     * @param logRepository 日志仓库接口
     */
    @Autowired
    public LogService(LogRepository logRepository, LogConfig logConfig, ElasticsearchClient client) {
        this.logRepository = logRepository;
        this.restTemplate = new RestTemplate();
        this.logConfig = logConfig;
        this.client = client;
    }

    /**
     * 记录新日志
     *
     * @param message 消息队列中获取的
     */
    public void createLog(String message) throws JsonProcessingException {
        log.info("Received log: {}", message);

        Map map = objectMapper.readValue(message, Map.class);
        LogRecord logEntry = LogParser.parseLogLine(String.valueOf(map.get("data")));
        assert logEntry != null;
        logEntry.setLevel(setResult(map.get("result")));

        this.logRepository.save(logEntry);
        log.info("Log saved: {}", logEntry);
    }

    private String setResult(Object result) {
        assert result != null;

        return switch (result.toString()) {
            case "true" -> "error";
            case "false" -> "info";
            default -> "network_error";
        };
    }

    /**
     * 查询特定级别在指定时间范围内的日志
     *
     * @param level     日志级别
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return 符合条件的日志列表
     */
    public List<LogRecord> findLogsByLevelAndTimeRange(String level, Date startDate, Date endDate) {
        return logRepository.findByLevelAndTimestampBetween(level, startDate, endDate);
    }

    /**
     * @param userId 用户id
     * @param page   页
     * @param size   大小
     * @param status 状态
     * @param host   主机名
     * @return 分页结果
     */
    public PageResult<LogInfo> listLogs(String userId, @Min(1) Integer page, Integer size, String status, String host) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        log.info("用户 {} 请求查看日志 page: {} size: {} status: {} host: {}", userId, page, size, status, host);
        Page<LogRecord> pages;

        if (status != null && host != null) {
            pages = logRepository.findByLevelAndHost(status.toLowerCase(Locale.ROOT), host, pageRequest);
        } else if (status != null) {
            pages = logRepository.findByLevel(status.toLowerCase(Locale.ROOT), pageRequest);
        } else if (host != null) {
            pages = logRepository.findByHost(host, pageRequest);
        } else {
            pages = logRepository.findBy(pageRequest);
        }

        List<LogInfo> list = pages.getContent()
                .stream().map(
                        log -> new LogInfo(
                                log.getId(),
                                log.getHost(),
                                log.getSource(),
                                log.getMessage(),
                                log.getLevel(),
                                log.getTimestamp()
                        )
                ).toList();

        PageResult<LogInfo> pageResult = new PageResult<>();
        pageResult.setTotal(pages.getTotalElements());
        pageResult.setPage(pages.getNumber() + 1);
        pageResult.setSize(pages.getSize());
        pageResult.setList(list);
        return pageResult;
    }

    public AnalysisLogResultVO analyze(String userId) throws IOException {
        SearchResponse<Void> search = client.search(s -> s
                        .index("logs")
                        .size(0)
                        .aggregations("group_by_level", a -> a
                                .terms(t -> t
                                        .field("level")
                                        .size(10)
                                )
                        ),
                Void.class);
        List<StringTermsBucket> buckets = search.aggregations()
                .get("group_by_level")
                .sterms()
                .buckets()
                .array();

        AtomicLong total = new AtomicLong(0);
        Map<String, Long> counts = new HashMap<>();
        buckets.forEach(bucket -> {
            total.addAndGet(bucket.docCount());
            counts.put(bucket.key()._get().toString(), bucket.docCount());
        });

        log.info("用户 {} 日志分析 {} ", userId, counts);
        return new AnalysisLogResultVO(
                total.get(),
                counts
        );
    }


    public static class LogParser {
        private static final Pattern LOG_PATTERN = Pattern.compile(
                "^\\[(?<file>[^]]+)] " +          // 捕获日志文件路径
                        "(?<timestamp>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+\\+\\d{2}:\\d{2}) " +
                        "(?<hostname>\\S+) " +            // 捕获主机名
                        "(?<message>.*)$"                 // 捕获日志信息
        );

        private static final DateTimeFormatter DATE_FORMATTER =
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX");

        public static LogRecord parseLogLine(String logLine) {
            Matcher matcher = LOG_PATTERN.matcher(logLine);
            if (matcher.find()) {
                LogRecord logRecord = new LogRecord();
                String timestampStr = matcher.group("timestamp");
                DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
                LocalDateTime dateTime = LocalDateTime.parse(timestampStr, formatter);
                logRecord.setTimestamp(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()));

                logRecord.setHost(matcher.group("hostname"));
                logRecord.setMessage(matcher.group("message"));
                logRecord.setSource(matcher.group("file"));
                return logRecord;
            }
            return null;
        }
    }

    @EventListener(ContextRefreshedEvent.class)
    public void handleContextRefreshed() {
        restTemplate.getForEntity(logConfig.getStartUrl(), Object.class);
        log.info("开启log {}", logConfig.getStartUrl());
    }

    @EventListener(ContextClosedEvent.class)
    public void handleContextClosed() {
        // 上下文关闭事件处理
        restTemplate.getForEntity(logConfig.getEndUrl(), Object.class);
        log.info("关闭log {}", logConfig.getEndUrl());
    }
}