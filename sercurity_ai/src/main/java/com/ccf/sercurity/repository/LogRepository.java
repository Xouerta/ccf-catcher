package com.ccf.sercurity.repository;

import com.ccf.sercurity.model.LogRecord;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Date;
import java.util.List;

public interface LogRepository extends ElasticsearchRepository<LogRecord, String> {
    List<LogRecord> findByLevelAndTimestampBetween(String level, Date startDate, Date endDate);
    List<LogRecord> findByUsername(String username);
    List<LogRecord> findByIpAddress(String ipAddress);
} 