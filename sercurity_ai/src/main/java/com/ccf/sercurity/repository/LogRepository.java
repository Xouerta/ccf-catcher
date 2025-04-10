package com.ccf.sercurity.repository;

import com.ccf.sercurity.model.LogRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Date;
import java.util.List;

public interface LogRepository extends ElasticsearchRepository<LogRecord, String> {
    List<LogRecord> findByLevelAndTimestampBetween(String level, Date startDate, Date endDate);

    Page<LogRecord> findBy(Pageable pageable);

    Page<LogRecord> findByLevel(String level, Pageable pageable);

    Page<LogRecord> findByHost(String host, Pageable pageable);

    Page<LogRecord> findByLevelAndHost(String level, String host, Pageable pageable);

    @Query("{\n" +
            "  \"size\": 0,\n" +
            "  \"aggs\": {\n" +
            "    \"group_by_level\": {\n" +
            "      \"terms\": {\n" +
            "        \"field\": \"level\",\n" +
            "        \"size\": 10\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}")
    SearchHits<LogRecord> groupByLevel();

}