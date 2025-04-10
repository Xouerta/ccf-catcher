package com.ccf.sercurity.vo;

public record AnalysisDeepStudyLogResultVO(
        long attackCount,
        long safeCount,
        long totalLogs
) {
}