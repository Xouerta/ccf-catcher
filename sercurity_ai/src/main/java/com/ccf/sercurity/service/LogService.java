package com.ccf.sercurity.service;

import com.ccf.sercurity.model.LogRecord;
import com.ccf.sercurity.repository.LogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

    /**
     * 构造函数，注入日志仓库
     *
     * @param logRepository 日志仓库接口
     */
    @Autowired
    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    /**
     * 记录新日志
     * @param message   消息队列中获取的
     *
     */
    public void createLog(String message) throws JsonProcessingException {
        log.info("Received log: {}", message);

        Map map = objectMapper.readValue(message, Map.class);
        LogRecord logEntry = LogParser.parseLogLine(String.valueOf(map.get("data")));
        System.out.println(logEntry);
        logEntry.setLevel(Boolean.getBoolean(String.valueOf(map.get("result"))) ? "ERROR" : "INFO" );

        this.logRepository.save(logEntry);
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
        // 上下文刷新事件处理
    }

    @EventListener(ContextClosedEvent.class)
    public void handleContextClosed() {
        // 上下文关闭事件处理
    }
}