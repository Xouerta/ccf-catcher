package com.ccf.sercurity.service;

import com.ccf.sercurity.model.TrafficData;
import com.ccf.sercurity.repository.TrafficRepository;
import com.ccf.sercurity.vo.PageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;

/**
 * 流量分析服务类
 * 负责记录和分析网络流量数据
 */
@Slf4j
@Service
public class TrafficAnalysisService {

    /**
     * 流量数据仓库接口
     */
    private final TrafficRepository trafficRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 构造函数，注入流量仓库
     *
     * @param trafficRepository 流量数据仓库接口
     */
    @Autowired
    public TrafficAnalysisService(TrafficRepository trafficRepository) {
        this.trafficRepository = trafficRepository;
    }

    public void saveTrafficData(String message) throws JsonProcessingException {
        var map = objectMapper.readValue(message, HashMap.class);

        TrafficData trafficData = objectMapper.readValue(map.get("input").toString(), TrafficData.class);
        trafficData.setResult(map.get("result").toString());
        trafficData.setTimestamp(new Date());

        trafficRepository.save(trafficData);
        log.info("Traffic data saved: {}", trafficData);
    }


    public PageResult<TrafficData> listTraffic(String userId, Integer page, Integer size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");
        PageRequest pageRequest = PageRequest.of(page - 1, size, sort);

        log.info("用户 {} 请求查看流量", userId);

        Page<TrafficData> pages = trafficRepository.findBy(pageRequest);

        PageResult<TrafficData> pageResult = new PageResult<>();
        pageResult.setTotal(pages.getTotalElements());
        pageResult.setPage(pages.getNumber() + 1);
        pageResult.setSize(pages.getSize());
        pageResult.setList(pages.getContent());
        return pageResult;
    }
}