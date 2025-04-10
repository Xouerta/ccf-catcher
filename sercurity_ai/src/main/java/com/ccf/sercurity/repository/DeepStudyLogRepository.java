package com.ccf.sercurity.repository;

import com.ccf.sercurity.model.DeepStudyLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface DeepStudyLogRepository extends ElasticsearchRepository<DeepStudyLog, String> {
    Page<DeepStudyLog> findByAttackDetectedAndHost(Boolean attack, String host, PageRequest pageRequest);

    Page<DeepStudyLog> findBy(PageRequest pageRequest);

    Page<DeepStudyLog> findByAttackDetected(Boolean attack, PageRequest pageRequest);

    Page<DeepStudyLog> findByHost(String host, PageRequest pageRequest);

    long countByAttackDetected(Boolean attack);
}
