// src/main/java/com/ccf/sercurity/controller/TrafficController.java
package com.ccf.sercurity.controller;

import com.ccf.sercurity.model.TrafficData;
import com.ccf.sercurity.service.TrafficAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/traffic")
public class TrafficController {

    @Autowired
    private TrafficAnalysisService trafficAnalysisService;

    /**
     * 记录新的流量数据
     *
     * @param sourceIp           源IP地址
     * @param destinationIp      目标IP地址
     * @param sourcePort         源端口
     * @param destinationPort    目标端口
     * @param protocol           协议
     * @param bytesTransferred   传输字节数
     * @param packetsTransferred 传输数据包数
     * @param httpMethod         HTTP方法(可选)
     * @param requestUri         请求URI(可选)
     * @param responseCode       响应状态码(可选)
     * @param additionalInfo     附加信息(可选)
     * @return 保存的流量数据记录
     */
    @PostMapping("/record")
    public ResponseEntity<TrafficData> recordTraffic(
            @RequestParam String sourceIp,
            @RequestParam String destinationIp,
            @RequestParam int sourcePort,
            @RequestParam int destinationPort,
            @RequestParam String protocol,
            @RequestParam long bytesTransferred,
            @RequestParam long packetsTransferred,
            @RequestParam(required = false) String httpMethod,
            @RequestParam(required = false) String requestUri,
            @RequestParam(required = false) int responseCode,
            @RequestParam(required = false) Map<String, Object> additionalInfo) {

        TrafficData trafficData = trafficAnalysisService.recordTraffic(
                sourceIp, destinationIp, sourcePort, destinationPort, protocol,
                bytesTransferred, packetsTransferred, httpMethod, requestUri, responseCode, additionalInfo);

        return ResponseEntity.ok(trafficData);
    }

    /**
     * 查询特定源IP在指定时间范围内的流量数据
     *
     * @param sourceIp  源IP地址
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return 符合条件的流量数据列表
     */
    @GetMapping("/find-by-source-ip-and-time-range")
    public ResponseEntity<List<TrafficData>> findTrafficBySourceIpAndTimeRange(
            @RequestParam String sourceIp,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate) {

        List<TrafficData> trafficDataList = trafficAnalysisService.findTrafficBySourceIpAndTimeRange(sourceIp, startDate, endDate);
        return ResponseEntity.ok(trafficDataList);
    }

    /**
     * 查询所有被标记为可疑的流量记录
     *
     * @return 可疑流量数据列表
     */
    @GetMapping("/find-all-suspicious")
    public ResponseEntity<List<TrafficData>> findAllSuspiciousTraffic() {
        List<TrafficData> suspiciousTrafficDataList = trafficAnalysisService.findAllSuspiciousTraffic();
        return ResponseEntity.ok(suspiciousTrafficDataList);
    }

    /**
     * 查询特定HTTP状态码以上的所有流量记录
     *
     * @param responseCode HTTP状态码阈值
     * @return 符合条件的流量数据列表
     */
    @GetMapping("/find-by-response-code-greater-than-equal")
    public ResponseEntity<List<TrafficData>> findTrafficByResponseCodeGreaterThanEqual(
            @RequestParam int responseCode) {

        List<TrafficData> trafficDataList = trafficAnalysisService.findTrafficByResponseCodeGreaterThanEqual(responseCode);
        return ResponseEntity.ok(trafficDataList);
    }
}
