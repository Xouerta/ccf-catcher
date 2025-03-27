package com.ccf.sercurity.repository;

import com.ccf.sercurity.model.LogRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Date;
import java.util.List;

public interface LogRepository extends ElasticsearchRepository<LogRecord, String> {
    List<LogRecord> findByLevelAndTimestampBetween(String level, Date startDate, Date endDate);

    Page<LogRecord> findBy(Pageable pageable);
}