package com.ccf.sercurity.repository;

import com.ccf.sercurity.model.FileInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface FileRepository extends ElasticsearchRepository<FileInfo, String> {
    List<FileInfo> findByIsMalicious(boolean isMalicious);

    @Query("{\"bool\": {\"must\": [{\"match\": {\"uploadFileUserId\": \"?0\"}}]}}")
    Page<FileInfo> searchFileInfoByUploadFileUserId(String userId, Pageable pageable);

    Page<FileInfo> findBy(Pageable pageable);
}