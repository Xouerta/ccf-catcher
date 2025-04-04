package com.ccf.sercurity.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private long flowDuration;

    @Field(type = FieldType.Integer)
    @JsonProperty("DestinationPort")
    private int destinationPort;

    // ================= 流量统计特征 =================
    @Field(type = FieldType.Long)
    @JsonProperty("TotalFwdPackets")
    private long totalFwdPackets;

    @Field(type = FieldType.Long)
    @JsonProperty("TotalBackwardPackets")
    private long totalBackwardPackets;

    @Field(type = FieldType.Long)
    @JsonProperty("TotalLengthOfFwdPackets")
    private long totalLengthOfFwdPackets;

    @Field(type = FieldType.Long)
    @JsonProperty("TotalLengthOfBwdPackets")
    private long totalLengthOfBwdPackets;

    // ================= 包长度统计 =================
    @Field(type = FieldType.Double)
    @JsonProperty("FwdPacketLengthMax")
    private double fwdPacketLengthMax;

    @Field(type = FieldType.Double)
    @JsonProperty("FwdPacketLengthMin")
    private double fwdPacketLengthMin;

    @Field(type = FieldType.Double)
    @JsonProperty("FwdPacketLengthMean")
    private double fwdPacketLengthMean;

    @Field(type = FieldType.Double)
    @JsonProperty("FwdPacketLengthStd")
    private double fwdPacketLengthStd;

    @Field(type = FieldType.Double)
    @JsonProperty("BwdPacketLengthMax")
    private double bwdPacketLengthMax;

    @Field(type = FieldType.Double)
    @JsonProperty("BwdPacketLengthMin")
    private double bwdPacketLengthMin;

    @Field(type = FieldType.Double)
    @JsonProperty("BwdPacketLengthMean")
    private double bwdPacketLengthMean;

    @Field(type = FieldType.Double)
    @JsonProperty("BwdPacketLengthStd")
    private double bwdPacketLengthStd;

    // ================= 流量速率特征 =================
    @Field(type = FieldType.Double)
    @JsonProperty("FlowBytes")
    private double flowBytes;

    @Field(type = FieldType.Double)
    @JsonProperty("FlowPackets")
    private double flowPackets;

    // ================= 时间间隔特征 =================
    @Field(type = FieldType.Double)
    @JsonProperty("FlowIATMean")
    private double flowIATMean;

    @Field(type = FieldType.Double)
    @JsonProperty("FlowIATStd")
    private double flowIATStd;

    @Field(type = FieldType.Double)
    @JsonProperty("FlowIATMax")
    private double flowIATMax;

    @Field(type = FieldType.Double)
    @JsonProperty("FlowIATMin")
    private double flowIATMin;

    @Field(type = FieldType.Double)
    @JsonProperty("FwdIATTotal")
    private double fwdIATTotal;

    @Field(type = FieldType.Double)
    @JsonProperty("FwdIATMean")
    private double fwdIATMean;

    @Field(type = FieldType.Double)
    @JsonProperty("FwdIATStd")
    private double fwdIATStd;

    @Field(type = FieldType.Double)
    @JsonProperty("FwdIATMax")
    private double fwdIATMax;

    @Field(type = FieldType.Double)
    @JsonProperty("FwdIATMin")
    private double fwdIATMin;

    @Field(type = FieldType.Double)
    @JsonProperty("BwdIATTotal")
    private double bwdIATTotal;

    @Field(type = FieldType.Double)
    @JsonProperty("BwdIATMean")
    private double bwdIATMean;

    @Field(type = FieldType.Double)
    @JsonProperty("BwdIATStd")
    private double bwdIATStd;

    @Field(type = FieldType.Double)
    @JsonProperty("BwdIATMax")
    private double bwdIATMax;

    @Field(type = FieldType.Double)
    @JsonProperty("BwdIATMin")
    private double bwdIATMin;

    // ================= TCP标志位统计 =================
    @Field(type = FieldType.Integer)
    @JsonProperty("FwdPSHFlags")
    private int fwdPSHFlags;

    @Field(type = FieldType.Integer)
    @JsonProperty("BwdPSHFlags")
    private int bwdPSHFlags;

    @Field(type = FieldType.Integer)
    @JsonProperty("FwdURGFlags")
    private int fwdURGFlags;

    @Field(type = FieldType.Integer)
    @JsonProperty("BwdURGFlags")
    private int bwdURGFlags;

    @Field(type = FieldType.Double)
    @JsonProperty("FwdHeaderLength")
    private double fwdHeaderLength;

    @Field(type = FieldType.Double)
    @JsonProperty("BwdHeaderLength")
    private double bwdHeaderLength;

    @Field(type = FieldType.Double)
    @JsonProperty("FwdPackets")
    private double fwdPackets;

    @Field(type = FieldType.Double)
    @JsonProperty("BwdPackets")
    private double bwdPackets;

    @Field(type = FieldType.Double)
    @JsonProperty("MinPacketLength")
    private double minPacketLength;

    @Field(type = FieldType.Double)
    @JsonProperty("MaxPacketLength")
    private double maxPacketLength;

    @Field(type = FieldType.Double)
    @JsonProperty("PacketLengthMean")
    private double packetLengthMean;

    @Field(type = FieldType.Double)
    @JsonProperty("PacketLengthStd")
    private double packetLengthStd;

    @Field(type = FieldType.Double)
    @JsonProperty("PacketLengthVariance")
    private double packetLengthVariance;

    @Field(type = FieldType.Integer)
    @JsonProperty("FINFlagCount")
    private int finFlagCount;

    @Field(type = FieldType.Integer)
    @JsonProperty("SYNFlagCount")
    private int synFlagCount;

    @Field(type = FieldType.Integer)
    @JsonProperty("RSTFlagCount")
    private int rstFlagCount;

    @Field(type = FieldType.Integer)
    @JsonProperty("PSHFlagCount")
    private int pshFlagCount;

    @Field(type = FieldType.Integer)
    @JsonProperty("ACKFlagCount")
    private int ackFlagCount;

    @Field(type = FieldType.Integer)
    @JsonProperty("URGFlagCount")
    private int urgFlagCount;

    @Field(type = FieldType.Integer)
    @JsonProperty("CWEFlagCount")
    private int cweFlagCount;

    @Field(type = FieldType.Integer)
    @JsonProperty("ECEFlagCount")
    private int eceFlagCount;

    @Field(type = FieldType.Double)
    @JsonProperty("DownUpRatio")
    private double downUpRatio;

        // ================= 包大小特征 =================
    @Field(type = FieldType.Double)
    @JsonProperty("AveragePacketSize")
    private double averagePacketSize;

    @Field(type = FieldType.Double)
    @JsonProperty("AvgFwdSegmentSize")
    private double avgFwdSegmentSize;

    @Field(type = FieldType.Double)
    @JsonProperty("AvgBwdSegmentSize")
    private double avgBwdSegmentSize;

    @Field(type = FieldType.Double)
    @JsonProperty("FwdHeaderLength1")
    private double fwdHeaderLength1;

    // ================= 批量传输特征 =================
    @Field(type = FieldType.Double)
    @JsonProperty("FwdAvgBytesBulk")
    private double fwdAvgBytesBulk;

    @Field(type = FieldType.Double)
    @JsonProperty("FwdAvgPacketsBulk")
    private double fwdAvgPacketsBulk;

    @Field(type = FieldType.Double)
    @JsonProperty("FwdAvgBulkRate")
    private double fwdAvgBulkRate;

    @Field(type = FieldType.Double)
    @JsonProperty("BwdAvgBytesBulk")
    private double bwdAvgBytesBulk;

    @Field(type = FieldType.Double)
    @JsonProperty("BwdAvgPacketsBulk")
    private double bwdAvgPacketsBulk;

    @Field(type = FieldType.Double)
    @JsonProperty("BwdAvgBulkRate")
    private double bwdAvgBulkRate;

    // ================= 子流特征 =================
    @Field(type = FieldType.Double)
    @JsonProperty("SubflowFwdPackets")
    private double subflowFwdPackets;

    @Field(type = FieldType.Double)
    @JsonProperty("SubflowFwdBytes")
    private double subflowFwdBytes;

    @Field(type = FieldType.Double)
    @JsonProperty("SubflowBwdPackets")
    private double subflowBwdPackets;

    @Field(type = FieldType.Double)
    @JsonProperty("SubflowBwdBytes")
    private double subflowBwdBytes;

    // ================= 窗口大小特征 =================
    @Field(type = FieldType.Double)
    @JsonProperty("Init_Win_bytes_forward")
    private double initWinBytesForward;

    @Field(type = FieldType.Double)
    @JsonProperty("Init_Win_bytes_backward")
    private double initWinBytesBackward;

    // ================= 行为分析特征 =================
    @Field(type = FieldType.Double)
    @JsonProperty("act_data_pkt_fwd")
    private double actDataPktFwd;

    @Field(type = FieldType.Double)
    @JsonProperty("min_seg_size_forward")
    private double minSegSizeForward;

    // ================= 时间统计特征 =================
    @Field(type = FieldType.Double)
    @JsonProperty("ActiveMean")
    private double activeMean;

    @Field(type = FieldType.Double)
    @JsonProperty("ActiveStd")
    private double activeStd;

    @Field(type = FieldType.Double)
    @JsonProperty("ActiveMax")
    private double activeMax;

    @Field(type = FieldType.Double)
    @JsonProperty("ActiveMin")
    private double activeMin;

    @Field(type = FieldType.Double)
    @JsonProperty("IdleMean")
    private double idleMean;

    @Field(type = FieldType.Double)
    @JsonProperty("IdleStd")
    private double idleStd;

    @Field(type = FieldType.Double)
    @JsonProperty("IdleMax")
    private double idleMax;

    @Field(type = FieldType.Double)
    @JsonProperty("IdleMin")
    private double idleMin;
}