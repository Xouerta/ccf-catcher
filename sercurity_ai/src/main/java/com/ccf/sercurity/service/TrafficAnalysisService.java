package com.ccf.sercurity.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.ccf.sercurity.model.TrafficData;
import com.ccf.sercurity.model.enums.WebsocketTypeEnum;
import com.ccf.sercurity.repository.TrafficRepository;
import com.ccf.sercurity.vo.AnalysisTrafficResultVO;
import com.ccf.sercurity.vo.PageResult;
import com.ccf.sercurity.vo.WebsocketPushVO;
import com.ccf.sercurity.websocket.WebSocketServer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

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

    private final ElasticsearchClient client;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 构造函数，注入流量仓库
     *
     * @param trafficRepository 流量数据仓库接口
     */
    @Autowired
    public TrafficAnalysisService(TrafficRepository trafficRepository, ElasticsearchClient client) {
        this.trafficRepository = trafficRepository;
        this.client = client;
    }

    public void saveTrafficData(String message) throws JsonProcessingException {
        var map = objectMapper.readValue(message, HashMap.class);

        TrafficData trafficData = objectMapper.readValue(map.get("input").toString(), TrafficData.class);
        trafficData.setResult(map.get("result").toString());
        trafficData.setTimestamp(new Date());

        trafficRepository.save(trafficData);
        log.info("Traffic data saved & websocket: {}", trafficData);

        WebsocketPushVO<TrafficData> vo = new WebsocketPushVO<>();
        vo.setCode(HttpStatus.OK.value())
                .setType(WebsocketTypeEnum.TRAFFIC.getType())
                .setData(trafficData);
        WebSocketServer.sendMessage(vo);
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

    public AnalysisTrafficResultVO analyze(String userId) throws IOException {
        SearchResponse<Void> search = client.search(s -> s
                        .index("traffic")
                        .size(0)
                        .aggregations("group_by_result", a -> a
                                .terms(t -> t
                                        .field("result")
                                        .size(10)
                                )
                        ),
                Void.class);
        List<StringTermsBucket> buckets = search.aggregations()
                .get("group_by_result")
                .sterms()
                .buckets()
                .array();

        AtomicLong total = new AtomicLong(0);

        Map<String, Long> counts = new HashMap<>();
        buckets.forEach(bucket -> {
            total.addAndGet(bucket.docCount());
            counts.put(bucket.key()._get().toString(), bucket.docCount());
        });

        log.info("用户 {} 请求流量分析 {} ", userId, counts);
        return new AnalysisTrafficResultVO(
                total.get(),
                counts
        );
    }
}