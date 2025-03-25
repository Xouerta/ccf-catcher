package com.ccf.sercurity.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

/**
 * 文件信息实体类
 * 用于存储上传文件的元数据信息
 * 映射到Elasticsearch的files索引
 */
@Data
@Document(indexName = "files")
public class FileInfo {
    
    /**
     * 文件唯一标识符
     */
    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String uploadFileUserId;
    
    /**
     * 原始文件名
     */
    @Field(type = FieldType.Keyword)
    private String originalName;
    
    /**
     * 存储的文件名（包含UUID前缀以避免冲突）
     */
    @Field(type = FieldType.Keyword)
    private String storedName;
    
    /**
     * 文件在服务器上的存储路径
     */
    @Field(type = FieldType.Text)
    private String filePath;
    
    /**
     * 文件大小（字节）
     */
    @Field(type = FieldType.Long)
    private long fileSize;
    
    /**
     * 文件内容类型/MIME类型
     */
    @Field(type = FieldType.Keyword)
    private String contentType;
    
    /**
     * 文件上传时间
     */
    @Field(type = FieldType.Date)
    private Date uploadTime;
    
    /**
     * 文件是否被检测为恶意文件的标志
     */
    @Field(type = FieldType.Boolean)
    private boolean isMalicious;

    /**
     * 检测置信度
     */
    @Field(type = FieldType.Double)
    private Double confidence;

    /**
     * 检测时间
     */
    @Field(type = FieldType.Date)
    private Date detectionTime;
} 