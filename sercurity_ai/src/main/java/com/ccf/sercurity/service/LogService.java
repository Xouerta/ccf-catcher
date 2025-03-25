package com.ccf.sercurity.service;

import com.ccf.sercurity.model.LogRecord;
import com.ccf.sercurity.repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 日志服务类
 * 负责记录和查询系统日志
 */
@Service
public class LogService {

    /**
     * 日志记录仓库接口
     */
    private final LogRepository logRepository;

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
     * 
     * @param level 日志级别
     * @param message 日志消息
     * @param source 日志来源
     * @param username 用户名(可选)
     * @param ipAddress IP地址(可选)
     * @param metadata 额外元数据(可选)
     * @return 保存的日志记录
     */
    public LogRecord createLog(String level, String message, String source, 
                              String username, String ipAddress, Map<String, Object> metadata) {
        LogRecord logRecord = new LogRecord();
        logRecord.setTimestamp(new Date());
        logRecord.setLevel(level);
        logRecord.setMessage(message);
        logRecord.setSource(source);
        logRecord.setUsername(username);
        logRecord.setIpAddress(ipAddress);
        logRecord.setMetadata(metadata);
        
        return logRepository.save(logRecord);
    }

    /**
     * 查询特定级别在指定时间范围内的日志
     * 
     * @param level 日志级别
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 符合条件的日志列表
     */
    public List<LogRecord> findLogsByLevelAndTimeRange(String level, Date startDate, Date endDate) {
        return logRepository.findByLevelAndTimestampBetween(level, startDate, endDate);
    }

    /**
     * 查询特定用户的日志记录
     * 
     * @param username 用户名
     * @return 该用户的日志列表
     */
    public List<LogRecord> findLogsByUsername(String username) {
        return logRepository.findByUsername(username);
    }

    /**
     * 查询特定IP地址的日志记录
     * 
     * @param ipAddress IP地址
     * @return 该IP的日志列表
     */
    public List<LogRecord> findLogsByIpAddress(String ipAddress) {
        return logRepository.findByIpAddress(ipAddress);
    }
} 