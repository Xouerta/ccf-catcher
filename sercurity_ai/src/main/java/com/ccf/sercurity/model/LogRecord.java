package com.ccf.sercurity.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.Map;

/**
 * 日志记录实体类
 * 用于存储系统日志信息
 * 映射到Elasticsearch的logs索引
 */
@Data
@Document(indexName = "logs")
public class LogRecord {

    /**
     * 日志唯一标识符
     */
    @Id
    private String id;

    /**
     * 日志记录时间戳
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date timestamp;

    /**
     * 日志级别（INFO, WARN, ERROR等）
     */
    @Field(type = FieldType.Keyword)
    private String level;

    /**
     * 日志详细信息
     */
    @Field(type = FieldType.Text)
    private String message;

    /**
     * 日志来源（服务名称、组件名称等）
     */
    @Field(type = FieldType.Keyword)
    private String source;

    /**
     * 执行操作的主机
     */
    @Field(type = FieldType.Keyword)
    private String host;

    /**
     * 其他元数据信息，可存储任意相关信息
     */
    @Field(type = FieldType.Object)
    private Map<String, Object> metadata;
} 