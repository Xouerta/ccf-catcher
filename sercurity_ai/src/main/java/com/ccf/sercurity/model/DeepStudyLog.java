package com.ccf.sercurity.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

@Data
@Document(indexName = "deep_study_log")
public class DeepStudyLog {

    @Id
    private String id;

    @Schema(name = "平均均方误差", example = "0.85")
    @Field(type = FieldType.Double)
    @JsonProperty("avg_mse")
    private double avgMse;

    @Schema(name = "是否检测到攻击", example = "true")
    @Field(type = FieldType.Boolean)
    @JsonProperty("attack_detected")
    private boolean attackDetected;

    @Schema(name = "原始日志文本", example = "2023-01-01T12:00:00.000+08:00 [WARN] Invalid access attempt")
    @Field(type = FieldType.Text)
    @JsonProperty("log_text")
    private String logText;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_fraction)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+8")
    @JsonProperty("timestamp")
    private Date timestamp;

    @Schema(name = "主机名称", example = "sun-WUJIE14XA")
    @Field(type = FieldType.Keyword)
    private String host;

    @Schema(name = "解析后的日志消息", example = "Can not open module: 没有那个文件或目录")
    @Field(type = FieldType.Text)
    private String logMessage;
}