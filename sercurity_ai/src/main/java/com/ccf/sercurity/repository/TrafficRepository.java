package com.ccf.sercurity.repository;

import com.ccf.sercurity.model.TrafficData;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Date;
import java.util.List;

public interface TrafficRepository extends ElasticsearchRepository<TrafficData, String> {
    List<TrafficData> findBySourceIpAndTimestampBetween(String sourceIp, Date startDate, Date endDate);

    List<TrafficData> findByIsSuspicious(boolean isSuspicious);

    List<TrafficData> findByResponseCodeGreaterThanEqual(int responseCode);
} 