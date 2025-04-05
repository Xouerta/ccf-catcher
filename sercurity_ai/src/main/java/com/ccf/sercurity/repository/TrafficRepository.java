package com.ccf.sercurity.repository;

import com.ccf.sercurity.model.TrafficData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface TrafficRepository extends ElasticsearchRepository<TrafficData, String> {
    Page<TrafficData> findBy(Pageable pageable);
} 