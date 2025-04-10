package com.ccf.sercurity.vo;

import java.util.Map;

public record AnalysisLogResultVO(
        long totalLogs,
        Map<String, Long> levelCounts
) {
}
