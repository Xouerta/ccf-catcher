package com.ccf.sercurity.repository;

import com.ccf.sercurity.model.FileInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.List;

public interface FileRepository extends ElasticsearchRepository<FileInfo, String> {
    List<FileInfo> findByIsMalicious(boolean isMalicious);
}