package com.ccf.sercurity.repository;

import com.ccf.sercurity.model.FileInfo;
import com.ccf.sercurity.vo.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.List;

public interface FileRepository extends ElasticsearchRepository<FileInfo, String> {
    List<FileInfo> findByIsMalicious(boolean isMalicious);

    @Query("{\"bool\": {\"must\": [{\"match\": {\"uploadFileUserId\": \"?0\"}}]}}")
    Page<FileInfo> searchFileInfoByUploadFileUserId(String userId, Pageable pageable);
}