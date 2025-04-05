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
@Document(indexName = "traffic")
public class TrafficData {

    @Id
    private String id;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date timestamp;

    @Field(type = FieldType.Keyword)
    private String result;


    // ================= 基础元数据 =================
    @Field(type = FieldType.Long)
    @JsonProperty("FlowDuration")
    @Schema(description = "流量持续时间")
    private long flowDuration;

    @Field(type = FieldType.Integer)
    @JsonProperty("DestinationPort")
    @Schema(description = "目的端口")
    private int destinationPort;

    // ================= 流量统计特征 =================
    @Field(type = FieldType.Long)
    @JsonProperty("TotalFwdPackets")
    @Schema(description = "正向流量包数量")
    private long totalFwdPackets;

    @Field(type = FieldType.Long)
    @JsonProperty("TotalBackwardPackets")
    @Schema(description = "反向流量包数量")
    private long totalBackwardPackets;

    @Field(type = FieldType.Long)
    @JsonProperty("TotalLengthOfFwdPackets")
    @Schema(description = "正向流量包总长度")
    private long totalLengthOfFwdPackets;

    @Field(type = FieldType.Long)
    @JsonProperty("TotalLengthOfBwdPackets")
    @Schema(description = "反向流量包总长度")
    private long totalLengthOfBwdPackets;

    // ================= 包长度统计 =================
    @Field(type = FieldType.Double)
    @JsonProperty("FwdPacketLengthMax")
    @Schema(description = "正向包长度最大值")
    private double fwdPacketLengthMax;

    @Field(type = FieldType.Double)
    @JsonProperty("FwdPacketLengthMin")
    @Schema(description = "反向包长度最小值")
    private double fwdPacketLengthMin;

    @Field(type = FieldType.Double)
    @JsonProperty("FwdPacketLengthMean")
    @Schema(description = "正向包长度平均值")
    private double fwdPacketLengthMean;

    @Field(type = FieldType.Double)
    @JsonProperty("FwdPacketLengthStd")
    @Schema(description = "反向包长度标准差")
    private double fwdPacketLengthStd;

    @Field(type = FieldType.Double)
    @JsonProperty("BwdPacketLengthMax")
    @Schema(description = "反向包长度最大值")
    private double bwdPacketLengthMax;

    @Field(type = FieldType.Double)
    @JsonProperty("BwdPacketLengthMin")
    @Schema(description = "反向包长度最小值")
    private double bwdPacketLengthMin;

    @Field(type = FieldType.Double)
    @JsonProperty("BwdPacketLengthMean")
    @Schema(description = "反向包长度平均值")
    private double bwdPacketLengthMean;

    @Field(type = FieldType.Double)
    @JsonProperty("BwdPacketLengthStd")
    @Schema(description = "反向包长度标准差")
    private double bwdPacketLengthStd;

    // ================= 流量速率特征 =================
    @Field(type = FieldType.Double)
    @JsonProperty("FlowBytes")
    @Schema(description = "流量字节数")
    private double flowBytes;

    @Field(type = FieldType.Double)
    @JsonProperty("FlowPackets")
    @Schema(description = "流量包数量")
    private double flowPackets;

    // ================= 时间间隔特征 =================
    @Field(type = FieldType.Double)
    @JsonProperty("FlowIATMean")
    @Schema(description = "流量IAT平均值")
    private double flowIATMean;

    @Field(type = FieldType.Double)
    @JsonProperty("FlowIATStd")
    @Schema(description = "流量IAT标准差")
    private double flowIATStd;

    @Field(type = FieldType.Double)
    @JsonProperty("FlowIATMax")
    @Schema(description = "流量IAT最大值")
    private double flowIATMax;

    @Field(type = FieldType.Double)
    @JsonProperty("FlowIATMin")
    @Schema(description = "流量IAT最小值")
    private double flowIATMin;

    @Field(type = FieldType.Double)
    @JsonProperty("FwdIATTotal")
    @Schema(description = "正向IAT总时间")
    private double fwdIATTotal;

    @Field(type = FieldType.Double)
    @JsonProperty("FwdIATMean")
    @Schema(description = "正向IAT平均值")
    private double fwdIATMean;

    @Field(type = FieldType.Double)
    @JsonProperty("FwdIATStd")
    @Schema(description = "正向IAT标准差")
    private double fwdIATStd;

    @Field(type = FieldType.Double)
    @JsonProperty("FwdIATMax")
    @Schema(description = "正向IAT最大值")
    private double fwdIATMax;

    @Field(type = FieldType.Double)
    @JsonProperty("FwdIATMin")
    @Schema(description = "正向IAT最小值")
    private double fwdIATMin;

    @Field(type = FieldType.Double)
    @JsonProperty("BwdIATTotal")
    @Schema(description = "反向IAT总时间")
    private double bwdIATTotal;

    @Field(type = FieldType.Double)
    @JsonProperty("BwdIATMean")
    @Schema(description = "反向IAT平均值")
    private double bwdIATMean;

    @Field(type = FieldType.Double)
    @JsonProperty("BwdIATStd")
    @Schema(description = "反向IAT标准差")
    private double bwdIATStd;

    @Field(type = FieldType.Double)
    @JsonProperty("BwdIATMax")
    @Schema(description = "反向IAT最大值")
    private double bwdIATMax;

    @Field(type = FieldType.Double)
    @JsonProperty("BwdIATMin")
    @Schema(description = "反向IAT最小值")
    private double bwdIATMin;

    // ================= TCP标志位统计 =================
    @Field(type = FieldType.Integer)
    @JsonProperty("FwdPSHFlags")
    @Schema(description = "正向PSH标志位数量")
    private int fwdPSHFlags;

    @Field(type = FieldType.Integer)
    @JsonProperty("BwdPSHFlags")
    @Schema(description = "反向PSH标志位数量")
    private int bwdPSHFlags;

    @Field(type = FieldType.Integer)
    @JsonProperty("FwdURGFlags")
    @Schema(description = "正向URG标志位数量")
    private int fwdURGFlags;

    @Field(type = FieldType.Integer)
    @JsonProperty("BwdURGFlags")
    @Schema(description = "反向URG标志位数量")
    private int bwdURGFlags;

    @Field(type = FieldType.Double)
    @JsonProperty("FwdHeaderLength")
    @Schema(description = "正向头部长度")
    private double fwdHeaderLength;

    @Field(type = FieldType.Double)
    @JsonProperty("BwdHeaderLength")
    @Schema(description = "反向头部长度")
    private double bwdHeaderLength;

    @Field(type = FieldType.Double)
    @JsonProperty("FwdPackets")
    @Schema(description = "正向包数量")
    private double fwdPackets;

    @Field(type = FieldType.Double)
    @JsonProperty("BwdPackets")
    @Schema(description = "反向包数量")
    private double bwdPackets;

    @Field(type = FieldType.Double)
    @JsonProperty("MinPacketLength")
    @Schema(description = "最小包长度")
    private double minPacketLength;

    @Field(type = FieldType.Double)
    @JsonProperty("MaxPacketLength")
    @Schema(description = "最大包长度")
    private double maxPacketLength;

    @Field(type = FieldType.Double)
    @JsonProperty("PacketLengthMean")
    @Schema(description = "包长度平均值")
    private double packetLengthMean;

    @Field(type = FieldType.Double)
    @JsonProperty("PacketLengthStd")
    @Schema(description = "包长度标准差")
    private double packetLengthStd;

    @Field(type = FieldType.Double)
    @JsonProperty("PacketLengthVariance")
    @Schema(description = "包长度方差")
    private double packetLengthVariance;

    @Field(type = FieldType.Integer)
    @JsonProperty("FINFlagCount")
    @Schema(description = "FIN标志数量")
    private int finFlagCount;

    @Field(type = FieldType.Integer)
    @JsonProperty("SYNFlagCount")
    @Schema(description = "SYN标志数量")
    private int synFlagCount;

    @Field(type = FieldType.Integer)
    @JsonProperty("RSTFlagCount")
    @Schema(description = "RST标志数量")
    private int rstFlagCount;

    @Field(type = FieldType.Integer)
    @JsonProperty("PSHFlagCount")
    @Schema(description = "PSH标志数量")
    private int pshFlagCount;

    @Field(type = FieldType.Integer)
    @JsonProperty("ACKFlagCount")
    @Schema(description = "ACK标志数量")
    private int ackFlagCount;

    @Field(type = FieldType.Integer)
    @JsonProperty("URGFlagCount")
    @Schema(description = "URG标志数量")
    private int urgFlagCount;

    @Field(type = FieldType.Integer)
    @JsonProperty("CWEFlagCount")
    @Schema(description = "CWE标志数量")
    private int cweFlagCount;

    @Field(type = FieldType.Integer)
    @JsonProperty("ECEFlagCount")
    @Schema(description = "ECE标志数量")
    private int eceFlagCount;

    @Field(type = FieldType.Double)
    @JsonProperty("DownUpRatio")
    @Schema(description = "上传下载比")
    private double downUpRatio;

        // ================= 包大小特征 =================
    @Field(type = FieldType.Double)
    @JsonProperty("AveragePacketSize")
    @Schema(description = "平均包大小")
    private double averagePacketSize;

    @Field(type = FieldType.Double)
    @JsonProperty("AvgFwdSegmentSize")
    @Schema(description = "正向平均包大小")
    private double avgFwdSegmentSize;

    @Field(type = FieldType.Double)
    @JsonProperty("AvgBwdSegmentSize")
    @Schema(description = "反向平均包大小")
    private double avgBwdSegmentSize;

    @Field(type = FieldType.Double)
    @JsonProperty("FwdHeaderLength1")
    @Schema(description = "正向头部长度1")
    private double fwdHeaderLength1;

    // ================= 批量传输特征 =================
    @Field(type = FieldType.Double)
    @JsonProperty("FwdAvgBytesBulk")
    @Schema(description = "正向平均批量字节数")
    private double fwdAvgBytesBulk;

    @Field(type = FieldType.Double)
    @JsonProperty("FwdAvgPacketsBulk")
    @Schema(description = "正向平均批量包数")
    private double fwdAvgPacketsBulk;

    @Field(type = FieldType.Double)
    @JsonProperty("FwdAvgBulkRate")
    @Schema(description = "正向平均批量速率")
    private double fwdAvgBulkRate;

    @Field(type = FieldType.Double)
    @JsonProperty("BwdAvgBytesBulk")
    @Schema(description = "反向平均批量字节数")
    private double bwdAvgBytesBulk;

    @Field(type = FieldType.Double)
    @JsonProperty("BwdAvgPacketsBulk")
    @Schema(description = "反向平均批量包数")
    private double bwdAvgPacketsBulk;

    @Field(type = FieldType.Double)
    @JsonProperty("BwdAvgBulkRate")
    @Schema(description = "反向平均批量速率")
    private double bwdAvgBulkRate;

    // ================= 子流特征 =================
    @Field(type = FieldType.Double)
    @JsonProperty("SubflowFwdPackets")
    @Schema(description = "子流正向包数量")
    private double subflowFwdPackets;

    @Field(type = FieldType.Double)
    @JsonProperty("SubflowFwdBytes")
    @Schema(description = "子流正向字节数量")
    private double subflowFwdBytes;

    @Field(type = FieldType.Double)
    @JsonProperty("SubflowBwdPackets")
    @Schema(description = "子流反向包数量")
    private double subflowBwdPackets;

    @Field(type = FieldType.Double)
    @JsonProperty("SubflowBwdBytes")
    @Schema(description = "子流反向字节数量")
    private double subflowBwdBytes;

    // ================= 窗口大小特征 =================
    @Field(type = FieldType.Double)
    @JsonProperty("Init_Win_bytes_forward")
    @Schema(description = "初始正向窗口大小")
    private double initWinBytesForward;

    @Field(type = FieldType.Double)
    @JsonProperty("Init_Win_bytes_backward")
    @Schema(description = "初始反向窗口大小")
    private double initWinBytesBackward;

    // ================= 行为分析特征 =================
    @Field(type = FieldType.Double)
    @JsonProperty("act_data_pkt_fwd")
    @Schema(description = "正向活动数据包数量")
    private double actDataPktFwd;

    @Field(type = FieldType.Double)
    @JsonProperty("min_seg_size_forward")
    @Schema(description = "正向最小分段大小")
    private double minSegSizeForward;

    // ================= 时间统计特征 =================
    @Field(type = FieldType.Double)
    @JsonProperty("ActiveMean")
    @Schema(description = "活动平均时间")
    private double activeMean;

    @Field(type = FieldType.Double)
    @JsonProperty("ActiveStd")
    @Schema(description = "活动标准差")
    private double activeStd;

    @Field(type = FieldType.Double)
    @JsonProperty("ActiveMax")
    @Schema(description = "活动最大时间")
    private double activeMax;

    @Field(type = FieldType.Double)
    @JsonProperty("ActiveMin")
    @Schema(description = "活动最小时间")
    private double activeMin;

    @Field(type = FieldType.Double)
    @JsonProperty("IdleMean")
    @Schema(description = "空闲平均时间")
    private double idleMean;

    @Field(type = FieldType.Double)
    @JsonProperty("IdleStd")
    @Schema(description = "空闲标准差")
    private double idleStd;

    @Field(type = FieldType.Double)
    @JsonProperty("IdleMax")
    @Schema(description = "空闲最大时间")
    private double idleMax;

    @Field(type = FieldType.Double)
    @JsonProperty("IdleMin")
    @Schema(description = "空闲最小时间")
    private double idleMin;
}