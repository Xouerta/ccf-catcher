package com.ccf.sercurity.repository;

import com.ccf.sercurity.model.TrafficData;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface TrafficRepository extends ElasticsearchRepository<TrafficData, String> {
} 