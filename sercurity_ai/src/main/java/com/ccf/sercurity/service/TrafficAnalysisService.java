package com.ccf.sercurity.service;

import com.ccf.sercurity.model.TrafficData;
import com.ccf.sercurity.repository.TrafficRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 流量分析服务类
 * 负责记录和分析网络流量数据
 */
@Service
public class TrafficAnalysisService {

    /**
     * 流量数据仓库接口
     */
    private final TrafficRepository trafficRepository;

    /**
     * 构造函数，注入流量仓库
     *
     * @param trafficRepository 流量数据仓库接口
     */
    @Autowired
    public TrafficAnalysisService(TrafficRepository trafficRepository) {
        this.trafficRepository = trafficRepository;
    }

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
    public TrafficData recordTraffic(String sourceIp, String destinationIp,
                                     int sourcePort, int destinationPort,
                                     String protocol, long bytesTransferred,
                                     long packetsTransferred, String httpMethod,
                                     String requestUri, int responseCode,
                                     Map<String, Object> additionalInfo) {
        TrafficData trafficData = new TrafficData();
        trafficData.setTimestamp(new Date());
        trafficData.setSourceIp(sourceIp);
        trafficData.setDestinationIp(destinationIp);
        trafficData.setSourcePort(sourcePort);
        trafficData.setDestinationPort(destinationPort);
        trafficData.setProtocol(protocol);
        trafficData.setBytesTransferred(bytesTransferred);
        trafficData.setPacketsTransferred(packetsTransferred);
        trafficData.setHttpMethod(httpMethod);
        trafficData.setRequestUri(requestUri);
        trafficData.setResponseCode(responseCode);
        trafficData.setAdditionalInfo(additionalInfo);

        // 进行简单的可疑流量分析
        trafficData.setSuspicious(analyzeForSuspiciousActivity(trafficData));

        return trafficRepository.save(trafficData);
    }

    /**
     * 分析流量数据，检测是否为可疑活动
     *
     * @param trafficData 流量数据对象
     * @return 如果检测为可疑活动返回true，否则返回false
     */
    private boolean analyzeForSuspiciousActivity(TrafficData trafficData) {
        // 实现简单的可疑流量检测逻辑
        // 这里仅为示例，实际项目中应该实现更复杂的分析算法

        // 检查是否是常见恶意端口
        if (trafficData.getDestinationPort() == 22 || // SSH
                trafficData.getDestinationPort() == 3389 || // RDP
                trafficData.getDestinationPort() == 445) { // SMB
            return true;
        }

        // HTTP错误响应码检查
        if (trafficData.getResponseCode() >= 400) {
            return true;
        }

        // 大量数据传输检查
        if (trafficData.getBytesTransferred() > 10000000) { // 大于10MB
            return true;
        }

        return false;
    }

    /**
     * 查询特定源IP在指定时间范围内的流量数据
     *
     * @param sourceIp  源IP地址
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return 符合条件的流量数据列表
     */
    public List<TrafficData> findTrafficBySourceIpAndTimeRange(String sourceIp, Date startDate, Date endDate) {
        return trafficRepository.findBySourceIpAndTimestampBetween(sourceIp, startDate, endDate);
    }

    /**
     * 查询所有被标记为可疑的流量记录
     *
     * @return 可疑流量数据列表
     */
    public List<TrafficData> findAllSuspiciousTraffic() {
        return trafficRepository.findByIsSuspicious(true);
    }

    /**
     * 查询特定HTTP状态码以上的所有流量记录
     *
     * @param responseCode HTTP状态码阈值
     * @return 符合条件的流量数据列表
     */
    public List<TrafficData> findTrafficByResponseCodeGreaterThanEqual(int responseCode) {
        return trafficRepository.findByResponseCodeGreaterThanEqual(responseCode);
    }
} 