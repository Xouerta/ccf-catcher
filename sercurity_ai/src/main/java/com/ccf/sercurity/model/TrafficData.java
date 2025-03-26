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
 * 流量数据实体类
 * 用于存储网络流量信息，用于分析和监测可疑活动
 * 映射到Elasticsearch的traffic索引
 */
@Data
@Document(indexName = "traffic")
public class TrafficData {

    /**
     * 流量记录唯一标识符
     */
    @Id
    private String id;
    
    /**
     * 数据记录时间戳
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date timestamp;
    
    /**
     * 源IP地址
     */
    @Field(type = FieldType.Keyword)
    private String sourceIp;
    
    /**
     * 目标IP地址
     */
    @Field(type = FieldType.Keyword)
    private String destinationIp;
    
    /**
     * 源端口
     */
    @Field(type = FieldType.Integer)
    private int sourcePort;
    
    /**
     * 目标端口
     */
    @Field(type = FieldType.Integer)
    private int destinationPort;
    
    /**
     * 网络协议（TCP, UDP, ICMP等）
     */
    @Field(type = FieldType.Keyword)
    private String protocol;
    
    /**
     * 传输的字节数
     */
    @Field(type = FieldType.Long)
    private long bytesTransferred;
    
    /**
     * 传输的数据包数量
     */
    @Field(type = FieldType.Long)
    private long packetsTransferred;
    
    /**
     * HTTP请求方法（GET, POST等，若适用）
     */
    @Field(type = FieldType.Keyword)
    private String httpMethod;
    
    /**
     * 请求URI（若适用）
     */
    @Field(type = FieldType.Text)
    private String requestUri;
    
    /**
     * HTTP响应状态码（若适用）
     */
    @Field(type = FieldType.Integer)
    private int responseCode;
    
    /**
     * 其他额外信息
     */
    @Field(type = FieldType.Object)
    private Map<String, Object> additionalInfo;
    
    /**
     * 是否为可疑流量的标志
     */
    @Field(type = FieldType.Boolean)
    private boolean isSuspicious;
} 