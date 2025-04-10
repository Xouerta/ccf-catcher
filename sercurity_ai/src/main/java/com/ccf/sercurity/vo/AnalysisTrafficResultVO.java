package com.ccf.sercurity.vo;

import java.util.Map;

public record AnalysisTrafficResultVO(
        long totalTraffic,
        Map<String, Long> levelCounts
) {
}
